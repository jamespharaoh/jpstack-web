package wbs.framework.object;

import wbs.framework.record.Record;

public
interface ObjectHelperUpdateMethods<RecordType extends Record<RecordType>> {

	RecordType createInstance ();

	<RecordTypeAgain extends Record<?>>
	RecordTypeAgain insert (
			RecordTypeAgain object);

	<RecordTypeAgain extends Record<?>>
	RecordTypeAgain insertSpecial (
			RecordTypeAgain object);

	<RecordTypeAgain extends Record<?>>
	RecordTypeAgain update (
			RecordTypeAgain object);

	<RecordTypeAgain extends Record<?>>
	RecordTypeAgain remove (
			RecordTypeAgain object);

	<RecordTypeAgain extends Record<?>>
	RecordTypeAgain lock (
			RecordTypeAgain object);

	void createSingletons (
			ObjectHelper<?> parentHelper,
			Record<?> parentObject);

}
