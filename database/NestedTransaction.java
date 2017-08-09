package wbs.framework.database;

import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.logging.LogContext;
import wbs.framework.logging.ParentTaskLogger;
import wbs.framework.logging.RealTaskLogger;
import wbs.framework.logging.TaskLoggerDefault;

public
class NestedTransaction
	implements
		CloseableTransaction,
		TaskLoggerDefault {

	// state

	private final
	OwnedTransaction ownedTransaction;

	private final
	RealTaskLogger realTaskLogger;

	// constructors

	public
	NestedTransaction (
			@NonNull OwnedTransaction ownedTransaction,
			@NonNull RealTaskLogger realTaskLogger) {

		this.ownedTransaction =
			ownedTransaction;

		this.realTaskLogger =
			realTaskLogger;

	}

	// implementation

	@Override
	public
	RealTaskLogger realTaskLogger () {
		return realTaskLogger;
	}

	@Override
	public
	ParentTaskLogger parentTaskLogger () {
		return realTaskLogger;
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
					realTaskLogger ()),
				dynamicContextName,
				dynamicContextParameters,
				debugEnabled
			).realTaskLogger ());

	}

	@Override
	public
	void close () {
		realTaskLogger.close ();
	}

}
