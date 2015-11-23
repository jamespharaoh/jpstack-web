package wbs.services.ticket.core.console;

import wbs.console.forms.FormFieldSet;
import wbs.framework.record.Record;

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
