package wbs.framework.logging;

import lombok.NonNull;

public
interface ParentTaskLoggerDefault
	extends ParentTaskLoggerMethods {

	@Override
	default
	Boolean debugEnabled () {
		return parentTaskLogger ().debugEnabled ();
	}

	@Override
	default
	Long nesting () {
		return parentTaskLogger ().nesting ();
	}

	@Override
	default
	void increaseErrorCount () {
		parentTaskLogger ().increaseErrorCount ();
	}

	@Override
	default
	void increaseWarningCount () {
		parentTaskLogger ().increaseWarningCount ();
	}

	@Override
	default
	void increaseNoticeCount () {
		parentTaskLogger ().increaseNoticeCount ();
	}

	@Override
	default
	void increaseLogicCount () {
		parentTaskLogger ().increaseLogicCount ();
	}

	@Override
	default
	void increaseDebugCount () {
		parentTaskLogger ().increaseDebugCount ();
	}

	@Override
	default
	void writeFirstError () {
		parentTaskLogger ().writeFirstError ();
	}

	@Override
	default
	void addChild (
			@NonNull OwnedTaskLogger child) {

		parentTaskLogger ().addChild (
			child);

	}

}
