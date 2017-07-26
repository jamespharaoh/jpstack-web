package wbs.framework.logging;

import static wbs.utils.etc.Misc.doNothing;

import com.google.common.base.Optional;

import lombok.NonNull;

public final
class ParallelTaskLoggerImplementation
	implements
		RealTaskLogger,
		TaskLoggerDefault {

	// state

	private final
	RealTaskLogger target;

	// constructors

	public
	ParallelTaskLoggerImplementation (
			@NonNull RealTaskLogger target) {

		this.target =
			target;

	}

	// parent task logger implementation

	@Override
	public synchronized
	RealTaskLogger realTaskLogger () {
		return target;
	}

	@Override
	public
	ParentTaskLogger parentTaskLogger () {
		return this;
	}

	@Override
	public synchronized
	Optional <ParentTaskLogger> parentOptional () {
		return target.parentOptional ();
	}

	@Override
	public synchronized
	Boolean debugEnabled () {
		return target.debugEnabled ();
	}

	@Override
	public synchronized
	Long nesting () {
		return target.nesting ();
	}

	@Override
	public synchronized
	void increaseErrorCount () {
		target.increaseErrorCount ();
	}

	@Override
	public synchronized
	void increaseWarningCount () {
		target.increaseWarningCount ();
	}

	@Override
	public synchronized
	void increaseNoticeCount () {
		target.increaseNoticeCount ();

	}

	@Override
	public synchronized
	void increaseLogicCount () {
		target.increaseLogicCount ();
	}

	@Override
	public synchronized
	void increaseDebugCount () {
		target.increaseDebugCount ();
	}

	@Override
	public synchronized
	void writeFirstError () {
		target.writeFirstError ();
	}

	@Override
	public synchronized
	void addChild (
			@NonNull OwnedTaskLogger child) {

		target.addChild (
			child);

	}

	// safe closeable implementation

	@Override
	public
	void close () {
		doNothing ();
	}

}
