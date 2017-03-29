package wbs.platform.service.model;

import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

public
interface ServiceObjectHelperMethods {

	ServiceRec findOrCreate (
			TaskLogger parentTaskLogger,
			Record <?> parent,
			String typeCode,
			String code);

}