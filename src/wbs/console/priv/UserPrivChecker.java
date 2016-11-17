package wbs.console.priv;

import java.util.Collection;
import java.util.Map;

import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

public
interface UserPrivChecker {

	boolean canRecursive (
			Long privId);

	boolean canRecursive (
			GlobalId parentObjectId,
			String ... privCodes);

	boolean canRecursive (
			Class <? extends Record <?>> parentObjectClass,
			Long parentObjectId,
			String ... privCodes);

	boolean canRecursive (
			Record <?> object,
			String ... privCodes);

	boolean canSimple (
			GlobalId parentObjectId,
			String ... privCodes);

	boolean canSimple (
			Record <?> parentObject,
			String ... privCodes);

	boolean canRecursive (
			Map <Object, Collection <String>> map);

	boolean canGrant (
			Long privId);

	void refresh (
			TaskLogger taskLogger);

}
