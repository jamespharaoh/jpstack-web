package wbs.console.forms;

import wbs.framework.entity.record.Record;

public
interface FieldsProvider <
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
> {

	FormFieldSet <ObjectType> getStaticFields ();

	FormFieldSet <ObjectType> getFieldsForParent (
			ParentType parent);

	FormFieldSet <ObjectType> getFieldsForObject (
			ObjectType object);

}
