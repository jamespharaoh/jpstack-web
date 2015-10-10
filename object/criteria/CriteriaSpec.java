package wbs.platform.object.criteria;

import wbs.console.helper.ConsoleHelper;
import wbs.framework.record.Record;

public
interface CriteriaSpec {

	boolean evaluate (
			ConsoleHelper<?> objectHelper,
			Record<?> object);

}
