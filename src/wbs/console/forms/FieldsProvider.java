package wbs.console.forms;

import wbs.framework.entity.record.Record;

public
interface FieldsProvider<
	ObjectType extends Record<ObjectType>,
	ParentType extends Record<ParentType>
> {

	FormFieldSet getStaticFields ();

	FormFieldSet getFieldsForParent (
			ParentType parent);

	FormFieldSet getFieldsForObject (
			ObjectType object);

}
