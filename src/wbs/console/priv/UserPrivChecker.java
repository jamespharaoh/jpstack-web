package wbs.console.priv;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

public
interface UserPrivChecker {

	Long userIdRequired ();

	boolean canRecursive (
			TaskLogger parentTaskLogger,
			Long privId);

	boolean canRecursive (
			TaskLogger parentTaskLogger,
			GlobalId parentObjectId,
			String ... privCodes);

	boolean canRecursive (
			TaskLogger parentTaskLogger,
			Class <? extends Record <?>> parentObjectClass,
			Long parentObjectId,
			String ... privCodes);

	boolean canRecursive (
			TaskLogger parentTaskLogger,
			Record <?> object,
			String ... privCodes);

	boolean canSimple (
			TaskLogger parentTaskLogger,
			GlobalId parentObjectId,
			String ... privCodes);

	boolean canSimple (
			TaskLogger parentTaskLogger,
			Record <?> parentObject,
			String ... privCodes);

	boolean canRecursive (
			TaskLogger parentTaskLogger,
			Map <Object, Collection <String>> map);

	boolean canGrant (
			TaskLogger parentTaskLogger,
			Long privId);

	Set <Long> getObjectIds (
			TaskLogger parentTaskLogger,
			Long parentTypeId);

	Set <Long> getCanRecursiveObjectIds (
			TaskLogger parentTaskLogger,
			Long parentTypeId,
			String ... privCodes);

	void refresh (
			TaskLogger taskLogger);

}
