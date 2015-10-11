package wbs.platform.service.model;

import wbs.framework.record.Record;

public
interface ServiceObjectHelperMethods {

	ServiceRec findOrCreate (
			Record<?> parent,
			String typeCode,
			String code);

}