package wbs.framework.object;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

public
interface ObjectHelperUpdateMethods <
	RecordType extends Record <RecordType>
> {

	RecordType createInstance ();

	<RecordTypeAgain extends Record <?>>
	RecordTypeAgain insert (
			Transaction parentTransaction,
			RecordTypeAgain object);

	<RecordTypeAgain extends Record <?>>
	RecordTypeAgain insertSpecial (
			Transaction parentTransaction,
			RecordTypeAgain object);

	<RecordTypeAgain extends Record <?>>
	RecordTypeAgain update (
			Transaction parentTransaction,
			RecordTypeAgain object);

	<RecordTypeAgain extends Record <?>>
	RecordTypeAgain remove (
			Transaction parentTransaction,
			RecordTypeAgain object);

	<RecordTypeAgain extends Record <?>>
	RecordTypeAgain lock (
			Transaction parentTransaction,
			RecordTypeAgain object);

	void createSingletons (
			Transaction parentTransaction,
			ObjectHelper <?> parentHelper,
			Record <?> parentObject);

}
