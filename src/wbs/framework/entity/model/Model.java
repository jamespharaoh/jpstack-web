package wbs.framework.entity.model;

import wbs.framework.entity.record.Record;

public
interface Model <ObjectType extends Record <ObjectType>>
	extends ModelMethods <ObjectType> {

}
