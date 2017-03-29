package wbs.framework.object;

import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

public
interface ObjectHelperUpdateMethods <
	RecordType extends Record <RecordType>
> {

	RecordType createInstance ();

	<RecordTypeAgain extends Record <?>>
	RecordTypeAgain insert (
			TaskLogger parentTaskLogger,
			RecordTypeAgain object);

	<RecordTypeAgain extends Record <?>>
	RecordTypeAgain insertSpecial (
			TaskLogger parentTaskLogger,
			RecordTypeAgain object);

	<RecordTypeAgain extends Record <?>>
	RecordTypeAgain update (
			TaskLogger parentTaskLogger,
			RecordTypeAgain object);

	<RecordTypeAgain extends Record <?>>
	RecordTypeAgain remove (
			RecordTypeAgain object);

	<RecordTypeAgain extends Record <?>>
	RecordTypeAgain lock (
			RecordTypeAgain object);

	void createSingletons (
			TaskLogger parentTaskLogger,
			ObjectHelper <?> parentHelper,
			Record <?> parentObject);

}
