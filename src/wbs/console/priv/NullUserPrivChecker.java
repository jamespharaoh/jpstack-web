package wbs.console.priv;

import static wbs.utils.etc.Misc.doNothing;

import java.util.Collection;
import java.util.Map;

import lombok.NonNull;

import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

public
class NullUserPrivChecker
	implements UserPrivChecker {

	@Override
	public
	boolean canRecursive (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long privId) {

		return false;

	}

	@Override
	public
	boolean canRecursive (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull GlobalId parentObjectId,
			@NonNull String ... privCodes) {

		return false;

	}

	@Override
	public
	boolean canRecursive (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Class <? extends Record <?>> parentObjectClass,
			@NonNull Long parentObjectId,
			@NonNull String ... privCodes) {

		return false;

	}

	@Override
	public
	boolean canRecursive (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record <?> object,
			@NonNull String ... privCodes) {

		return false;

	}

	@Override
	public
	boolean canSimple (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull GlobalId parentObjectId,
			@NonNull String ... privCodes) {

		return false;

	}

	@Override
	public
	boolean canSimple (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record <?> parentObject,
			@NonNull String ... privCodes) {

		return false;

	}

	@Override
	public
	boolean canRecursive (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Map <Object, Collection <String>> map) {

		return false;

	}

	@Override
	public
	boolean canGrant (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long privId) {

		return false;

	}

	@Override
	public
	void refresh (
			@NonNull TaskLogger taskLogger) {

		doNothing ();

	}

}
