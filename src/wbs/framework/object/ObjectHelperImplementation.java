package wbs.framework.object;

import wbs.framework.entity.record.Record;

public
interface ObjectHelperImplementation <
	RecordType extends Record <RecordType>
> {

	void objectManager (
			ObjectManager objectManager);

	ObjectHelperChildrenImplementation <RecordType> childrenImplementation ();
	ObjectHelperCodeImplementation <RecordType> codeImplementation ();
	ObjectHelperFindImplementation <RecordType> findImplementation ();
	ObjectHelperIdImplementation <RecordType> idImplementation ();
	ObjectHelperIndexImplementation <RecordType> indexImplementation ();
	ObjectHelperModelImplementation <RecordType> modelImplementation ();
	ObjectHelperPropertyImplementation <RecordType> propertyImplementation ();
	ObjectHelperUpdateImplementation <RecordType> updateImplementation ();

}
