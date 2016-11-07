package wbs.platform.object.criteria;

import wbs.console.helper.core.ConsoleHelper;
import wbs.framework.entity.record.Record;

public
interface CriteriaSpec {

	boolean evaluate (
			ConsoleHelper<?> objectHelper,
			Record<?> object);

}
