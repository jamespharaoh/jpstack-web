package wbs.console.forms.types;

import wbs.console.forms.core.FormFieldSet;

import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

public
interface FieldsProvider <
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
> {

	FormFieldSet <ObjectType> getStaticFields ();

	FormFieldSet <ObjectType> getFieldsForParent (
			TaskLogger taskLogger,
			ParentType parent);

	FormFieldSet <ObjectType> getFieldsForObject (
			TaskLogger taskLogger,
			ObjectType object);

}
