package wbs.platform.object.criteria;

import wbs.console.helper.core.ConsoleHelper;

import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

public
interface CriteriaSpec {

	boolean evaluate (
			TaskLogger parentTaskLogger,
			ConsoleHelper <?> objectHelper,
			Record <?> object);

}
