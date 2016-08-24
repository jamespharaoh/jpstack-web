package wbs.framework.object;

import wbs.framework.entity.record.Record;

public 
interface ObjectHelperComponent <RecordType extends Record <RecordType>> {

	ObjectHelperComponent <RecordType> objectHelper (
			ObjectHelper <RecordType> objectHelper);

	ObjectHelperComponent <RecordType> objectDatabaseHelper (
			ObjectDatabaseHelper <RecordType> objectDatabaseHelper);
		
	ObjectHelperComponent <RecordType> objectManager (
			ObjectManager objectManager);
		
	ObjectHelperComponent <RecordType> model (
			ObjectModel <RecordType> model);

	default
	ObjectHelperComponent <RecordType> setup () {
		return this;
	}

}
