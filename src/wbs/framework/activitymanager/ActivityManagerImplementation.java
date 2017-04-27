package wbs.framework.activitymanager;

import static wbs.utils.collection.MapUtils.mapIsEmpty;
import static wbs.utils.etc.LogicUtils.referenceNotEqualSafe;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.max;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.roundToIntegerRequired;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.notShorterThan;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.apache.commons.io.IOUtils;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.NormalLifecycleSetup;

import wbs.utils.io.RuntimeIoException;
import wbs.utils.string.FormatWriter;
import wbs.utils.string.StringFormatWriter;
import wbs.utils.string.WriterFormatWriter;

@Log4j
public
class ActivityManagerImplementation
	implements ActivityManager {

	// properties

	@Getter @Setter
	Duration slowTaskDuration =
		Duration.millis (
			1000);

	@Getter @Setter
	Duration showTaskDuration =
		Duration.millis (
			5);

	// state

	String hostname;
	int processId;
	long nextTaskId;

	ThreadLocal <Task> currentTask =
		new ThreadLocal<> ();

	Map <Long, Task> activeTasks =
		new LinkedHashMap<> ();

	// implementation

	@NormalLifecycleSetup
	public
	void init () {
		throw new RuntimeException ();
	}

	public
	ActivityManagerImplementation () {

		log.debug (
			stringFormat (
				"Initialising"));

		nextTaskId =
			new Random ().nextLong ();

		log.debug (
			stringFormat (
				"Next task id is %s",
				integerToDecimalString (
					nextTaskId)));

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
				integerToDecimalString (
					processId)));

	}

	int getProcessId () {

		try {

			return Integer.parseInt (
				new File ("/proc/self")
					.getCanonicalFile ()
					.getName ());

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	String getHostname () {

		try {

			Process process =
				Runtime.getRuntime ().exec ("hostname");

			String processOutput =
				IOUtils.toString (
					process.getInputStream ());

			String hostname =
				processOutput.trim ();

			return hostname;

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	@Override
	public synchronized
	ActiveTask start (
			@NonNull String taskType,
			@NonNull String summary,
			@NonNull Object owner) {

		return start (
			taskType,
			summary,
			owner,
			ImmutableMap.<String,String>of ());

	}

	@Override
	public synchronized
	ActiveTask start (
			@NonNull String taskType,
			@NonNull String summary,
			@NonNull Object owner,
			@NonNull Map<String,String> parameters) {

		log.debug (
			stringFormat (
				"Begin %s task: %s",
				taskType,
				summary));

		Task task =
			new Task ()

			.taskId (
				nextTaskId ++)

			.parent (
				currentTask.get ())

			.owner (
				owner)

			.taskType (
				taskType)

			.summary (
				summary)

			.hostname (
				hostname)

			.processId (
				processId)

			.threadName (
				Thread.currentThread ().getName ())

			.startTime (
				Instant.now ())

			.state (
				Task.State.active);

		task.parameters ().putAll (
			parameters);

		ActiveTask activeTask =
			new ActiveTaskImplementation (
				task);

		activeTasks.put (
			task.taskId (),
			task);

		if (
			isNotNull (
				task.parent ())
		) {

			task.parent ().children ().add (
				task);

		}

		currentTask.set (
			task);

		return activeTask;

	}

	private synchronized
	void postProcessTask (
			@NonNull Task task) {

		task.endTime (
			Instant.now ());

		if (
			referenceNotEqualSafe (
				task,
				currentTask.get ())
		) {

			logActiveTasks ();

			throw new RuntimeException (
				stringFormat (
					"End task %s (%s, %s) ",
					integerToDecimalString (
						task.taskId ()),
					task.taskType (),
					task.summary (),
					"but current task is %s (%s, %s)",
					integerToDecimalString (
						currentTask.get ().taskId ()),
					currentTask.get ().taskType (),
					currentTask.get ().summary ()));

		}

		Duration taskDuration =
			new Duration (
				task.startTime (),
				task.endTime ());

		log.debug (
			stringFormat (
				"End %s task %s after %s: %s",
				task.taskType (),
				task.summary (),
				taskDuration.toString (),
				task.state ().toString ()));

		if (

			isNull (
				task.parent ())

			&& notShorterThan (
				taskDuration,
				slowTaskDuration)

		) {

			StringWriter stringWriter =
				new StringWriter ();

			try (

				FormatWriter formatWriter =
					new WriterFormatWriter (
						stringWriter);

			) {

				formatWriter.writeFormat (
					"Slow %s task took %s: %s\n",
						task.taskType (),
						taskDuration.toString (),
						task.summary ());

				writeTaskRecursive (
					formatWriter,
					"  ",
					showTaskDuration,
					task);

				log.warn (
					stringWriter.toString ());

			}

		}

		activeTasks.remove (
			task.taskId ());

		currentTask.set (
			task.parent ());

	}

	public synchronized
	void logActiveTasks () {

		try (

			StringFormatWriter formatWriter =
				new StringFormatWriter ();

		) {

			if (
				mapIsEmpty (
					activeTasks)
			) {

				log.info (
					"No active tasks");

			} else {

				formatWriter.writeFormat (
					"Dumping active tasks\n");

				writeActiveTasks (
					formatWriter,
					"  ");

				log.info (
					formatWriter.toString ());

			}

		}

	}

	public synchronized
	void writeActiveTasks (
			@NonNull FormatWriter formatWriter,
			@NonNull String indent) {

		for (
			Task task
				: activeTasks.values ()
		) {

			if (
				isNotNull (
					task.parent ())
			) {
				continue;
			}

			writeActiveTaskRecursive (
				formatWriter,
				indent,
				task);

		}

	}

	public
	void writeTask (
			@NonNull FormatWriter writer,
			@NonNull String indent,
			@NonNull Task task) {

		long meterLength =
			max (
				0l,
				roundToIntegerRequired (
					Math.log (
						task.duration ().getMillis ())));

		char[] meterCharacters =
			new char [
				toJavaIntegerRequired (
					meterLength)];

		Arrays.fill (
			meterCharacters,
			'=');

		writer.writeFormat (
			"%s%s: %s (%sms) [%s]\n",
			indent,
			task.taskType (),
			task.summary (),
			integerToDecimalString (
				task.duration ().getMillis ()),
			new String (
				meterCharacters));

	}

	public
	void writeTaskRecursive (
			@NonNull FormatWriter writer,
			@NonNull String indent,
			@NonNull Duration minDuration,
			@NonNull Task task) {

		writeTask (
			writer,
			indent,
			task);

		String nextIndent =
			indent + "  ";

		task.children ().stream ()

			.filter (
				childTask ->
					notShorterThan (
						childTask.duration (),
						minDuration))

			.forEach (
				childTask ->
					writeTaskRecursive (
						writer,
						nextIndent,
						minDuration,
						childTask));

	}

	public
	void writeActiveTaskRecursive (
			@NonNull FormatWriter writer,
			@NonNull String indent,
			@NonNull Task task) {

		writeTask (
			writer,
			indent,
			task);

		String nextIndent =
			indent + "  ";

		task.children ().stream ()

			.filter (
				childTask ->
					isNull (
						childTask.endTime ()))

			.forEach (
				childTask ->
					writeActiveTaskRecursive (
						writer,
						nextIndent,
						childTask));

	}

	@Override
	public
	Task currentTask () {

		return currentTask.get ();

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

				postProcessTask (
					task);

				task.state (
					Task.State.success);

				concluded =
					true;

			}

		}

		@Override
		public <ExceptionType extends Throwable>
		ExceptionType fail (
				@NonNull ExceptionType exception) {

			synchronized (ActivityManagerImplementation.this) {

				if (concluded) {

					throw new IllegalStateException (
						"Tried to fail task in concluded state");

				}

				if (closed) {

					throw new IllegalStateException (
						"Tried to fail task in closed state");

				}

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

		@Override
		public
		ActiveTask put (
				@NonNull String key,
				@NonNull String value) {

			task.parameters.put (
				key,
				value);

			return this;

		}

	}

}
