package wbs.framework.object;

import lombok.NonNull;

import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

public
interface ObjectHelperComponent <RecordType extends Record <RecordType>> {

	ObjectHelperComponent <RecordType> objectModel (
			ObjectModel <RecordType> objectModel);

	ObjectHelperComponent <RecordType> objectHelper (
			ObjectHelper <RecordType> objectHelper);

	ObjectHelperComponent <RecordType> objectDatabaseHelper (
			ObjectDatabaseHelper <RecordType> objectDatabaseHelper);

	default
	ObjectHelperComponent <RecordType> setup (
			@NonNull TaskLogger parentTaskLogger) {

		return this;

	}

}
