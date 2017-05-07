package wbs.framework.logging;

import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.etc.LogicUtils.anyOf;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.base.Optional;

import lombok.NonNull;

public
class MultipleLogTarget
	implements LogTarget {

	// state

	private final
	List <LogTarget> logTargets;

	// constructors

	public
	MultipleLogTarget (
			@NonNull List <LogTarget> logTargets) {

		this.logTargets =
			logTargets;

	}

	// log target implementation

	@Override
	public
	void writeToLog (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull LogSeverity severity,
			@NonNull CharSequence message,
			@NonNull Optional <Throwable> exception) {

		logTargets.forEach (
			logTarget ->
				logTarget.writeToLog (
					parentTaskLogger,
					severity,
					message,
					exception));

	}

	@Override
	public
	Boolean debugEnabled () {

		return anyOf (
			iterableMap (
				logTarget ->
					(Supplier <Boolean>)
					() -> logTarget.debugEnabled (),
				logTargets));

	}

}
