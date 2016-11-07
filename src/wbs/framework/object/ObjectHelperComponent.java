package wbs.framework.object;

import wbs.framework.entity.record.Record;

public
interface ObjectHelperComponent <RecordType extends Record <RecordType>> {

	ObjectHelperComponent <RecordType> objectModel (
			ObjectModel <RecordType> objectModel);

	ObjectHelperComponent <RecordType> objectHelper (
			ObjectHelper <RecordType> objectHelper);

	ObjectHelperComponent <RecordType> objectDatabaseHelper (
			ObjectDatabaseHelper <RecordType> objectDatabaseHelper);

	default
	ObjectHelperComponent <RecordType> setup () {
		return this;
	}

}
