package wbs.framework.logging;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.collection.MapUtils.mapItemForKeyOrElseSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

public
class LoggingLogicImplementation
	implements LoggingLogic {

	// state

	Boolean debugEnabled;

	List <LogTargetFactory> logTargetFactories;

	Map <Long, TaskLogger> activeRootTasks =
		new TreeMap<> ();

	Map <Long, TaskLogger> inactiveRootTasks =
		new TreeMap<> ();

	Map <String, LogContext> logContexts =
		new HashMap<> ();

	// constructors

	public
	LoggingLogicImplementation (
			@NonNull Boolean debugEnabled,
			@NonNull List <LogTargetFactory> logTargetFactories) {

		this.debugEnabled =
			debugEnabled;

		this.logTargetFactories =
			ImmutableList.copyOf (
				logTargetFactories);

	}

	// logging logic implementation

	@Override
	public synchronized
	LogContext findOrCreateLogContext (
			@NonNull CharSequence staticContextName) {

		return mapItemForKeyOrElseSet (
			logContexts,
			staticContextName.toString (),
			() -> createLogContext (
				staticContextName));

	}

	// log target implementation

	@Override
	public
	void writeToLog (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull LogSeverity severity,
			@NonNull CharSequence message,
			@NonNull Optional <Throwable> exception) {

		//throw todo ();

	}

	@Override
	public
	Boolean debugEnabled () {
		return debugEnabled;
	}

	// private implementation

	private
	LogContext createLogContext (
			@NonNull CharSequence staticContextName) {

		LogTarget logTarget =
			new MultipleLogTarget (
				iterableMapToList (
					logTargetFactories,
					logTargetFactory ->
						logTargetFactory.createLogTarget (
							staticContextName)));

		return new LogContextImplementation (
			staticContextName,
			logTarget);

	}

}
