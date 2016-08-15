package wbs.framework.object;

import wbs.framework.record.Record;

public
interface ObjectHelperMethods<RecordType extends Record<RecordType>>
	extends
		ObjectHelperChildrenMethods<RecordType>,
		ObjectHelperCodeMethods<RecordType>,
		ObjectHelperFindMethods<RecordType>,
		ObjectHelperIdMethods<RecordType>,
		ObjectHelperIndexMethods<RecordType>,
		ObjectHelperModelMethods<RecordType>,
		ObjectHelperPropertyMethods<RecordType>,
		ObjectHelperUpdateMethods<RecordType> {

}
