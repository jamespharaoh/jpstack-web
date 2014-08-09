package wbs.platform.object.criteria;

import wbs.framework.record.Record;
import wbs.platform.console.helper.ConsoleHelper;

public
interface CriteriaSpec {

	boolean evaluate (
			ConsoleHelper<?> objectHelper,
			Record<?> object);

}
