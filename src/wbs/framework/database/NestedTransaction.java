package wbs.framework.database;

import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLoggerImplementation;

public
class NestedTransaction
	implements CloseableTransaction {

	// state

	private final
	OwnedTransaction ownedTransaction;

	private final
	TaskLoggerImplementation taskLoggerImplementation;

	// constructors

	public
	NestedTransaction (
			@NonNull OwnedTransaction ownedTransaction,
			@NonNull TaskLoggerImplementation taskLoggerImplementation) {

		this.ownedTransaction =
			ownedTransaction;

		this.taskLoggerImplementation =
			taskLoggerImplementation;

	}

	// implementation

	@Override
	public
	TaskLoggerImplementation taskLoggerImplementation () {
		return taskLoggerImplementation;
	}

	@Override
	public
	OwnedTransaction ownedTransaction () {
		return ownedTransaction;
	}

	@Override
	public
	void commit () {

		ownedTransaction.commit (
			this);

	}

	@Override
	public
	NestedTransaction nestTransaction (
			@NonNull LogContext logContext,
			@NonNull String dynamicContextName,
			@NonNull List <CharSequence> dynamicContextParameters,
			@NonNull Optional <Boolean> debugEnabled) {

		return new NestedTransaction (
			ownedTransaction (),
			logContext.nestTaskLogger (
				optionalOf (
					ownedTransaction ()),
				dynamicContextName,
				dynamicContextParameters,
				debugEnabled
			).taskLoggerImplementation ());

	}

	@Override
	public
	void close () {
		taskLoggerImplementation.close ();
	}

}
