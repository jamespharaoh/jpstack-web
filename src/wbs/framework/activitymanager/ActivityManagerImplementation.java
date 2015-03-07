package wbs.framework.activitymanager;

import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.apache.commons.io.IOUtils;
import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.utils.RandomLogic;

@Log4j
@SingletonComponent ("activityManager")
public
class ActivityManagerImplementation
	implements ActivityManager {

	// dependencies

	@Inject
	RandomLogic randomLogic;

	// properties

	@Getter @Setter
	Duration slowTaskDuration =
		Duration.millis (100);

	// state

	String hostname;
	int processId;
	long nextTaskId;

	Map<Long,Task> activeTasks =
		new LinkedHashMap<Long,Task> ();

	// implementation

	@PostConstruct
	public
	void init () {

		log.debug (
			stringFormat (
				"Initialising"));

		nextTaskId =
			randomLogic.randomInteger (
				Integer.MAX_VALUE
			) << 32;

		log.debug (
			stringFormat (
				"Next task id is %s",
				nextTaskId));

		hostname =
			getHostname ();

		log.debug (
			stringFormat (
				"Host name is %s",
				hostname));

		processId =
			getProcessId ();

		log.debug (
			stringFormat (
				"Process ID is %s",
				processId));

	}

	@SneakyThrows (IOException.class)
	int getProcessId () {

		return Integer.parseInt (
			new File ("/proc/self")
				.getCanonicalFile ()
				.getName ());

	}

	@SneakyThrows (IOException.class)
	String getHostname () {

		Process process =
			Runtime.getRuntime ().exec ("hostname");

		String processOutput =
			IOUtils.toString (
				process.getInputStream ());

		String hostname =
			processOutput.trim ();

		return hostname;

	}

	@Override
	public synchronized
	ActiveTask start (
			String taskName,
			Map<String,Object> parameters) {

		log.debug (
			stringFormat (
				"Begin task %s",
				taskName));

		Task task =
			new Task ()

			.taskId (
				nextTaskId ++)

			.taskName (
				taskName)

			.hostname (
				hostname)

			.processId (
				processId)

			.threadName (
				Thread.currentThread ().getName ())

			.startTime (
				Instant.now ())

			.parameters (
				parameters);

		ActiveTask activeTask =
			new ActiveTaskImplementation (
				task);

		activeTasks.put (
			task.taskId (),
			task);

		return activeTask;

	}

	private synchronized
	void postProcessTask (
			Task task) {

		task.endTime (
			Instant.now ());

		Duration taskDuration =
			new Duration (
				task.startTime (),
				task.endTime ());

		log.debug (
			stringFormat (
				"End task %s (%s) after %s",
				task.taskName (),
				task.state ().toString (),
				taskDuration.toString ()));

		if (
			taskDuration.isLongerThan (
				slowTaskDuration)
		) {

			log.warn (
				stringFormat (
					"Slow task %s took %s",
					task.taskName (),
					taskDuration));

		}

		activeTasks.remove (
			task.taskId ());

	}

	public synchronized
	void dump () {

		log.info (
			"Begin active task dump");

		for (Task task
				: activeTasks.values ()) {

			log.info (
				joinWithSeparator (
					", ",

					stringFormat (
						"id=%s",
						task.taskId ()),

					stringFormat (
						"name=%s",
						task.taskName ()),

					stringFormat (
						"host=%s",
						task.hostname ()),

					stringFormat (
						"pid=%s",
						task.processId ()),

					stringFormat (
						"thread=%s",
						task.threadName ()),

					stringFormat (
						"start=%s",
						task.startTime ()),

					stringFormat (
						"end=%s",
						task.endTime ())));

		}

		log.info (
			"End active task dump");

	}

	@Accessors (fluent = true)
	class ActiveTaskImplementation
		implements ActiveTask {

		Task task;

		boolean concluded = false;
		boolean closed = false;

		public
		ActiveTaskImplementation (
				Task task) {

			this.task =
				task;

		}

		@Override
		public
		void success () {

			synchronized (ActivityManagerImplementation.this) {

				if (concluded)
					throw new IllegalStateException ();

				if (closed)
					throw new IllegalStateException ();

				concluded =
					true;

				task.state (
					Task.State.success);

				postProcessTask (
					task);

			}

		}

		@Override
		public <ExceptionType extends Throwable>
		ExceptionType fail (
				ExceptionType exception) {

			synchronized (ActivityManagerImplementation.this) {

				if (concluded)
					throw new IllegalStateException ();

				if (closed)
					throw new IllegalStateException ();

				concluded =
					true;

				task.state (
					Task.State.failure);

				postProcessTask (
					task);

				return exception;

			}

		}

		@Override
		public
		void close () {

			synchronized (ActivityManagerImplementation.this) {

				if (closed)
					throw new IllegalStateException ();

				closed =
					true;

				if (concluded)
					return;

				task.state (
					Task.State.unknown);

				postProcessTask (
					task);

			}

		}

	}

}
