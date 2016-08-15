package wbs.framework.object;

import wbs.framework.entity.model.ModelMethods;
import wbs.framework.record.Record;

public
interface ObjectModel<RecordType extends Record<RecordType>>
	extends
		ObjectModelMethods<RecordType>,
		ModelMethods {

}
