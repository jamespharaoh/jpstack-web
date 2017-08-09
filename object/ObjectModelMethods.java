package wbs.framework.object;

import wbs.framework.entity.record.Record;

public
interface ObjectModelMethods <
	RecordType extends Record <RecordType>
> {

	Long objectTypeId ();
	String objectTypeCode ();

	Long parentTypeId ();
	Class <? extends Record <?>> parentClassRequired ();

	Object daoImplementation ();
	Class <?> daoInterface ();

	ObjectHooks <RecordType> hooks ();

}
