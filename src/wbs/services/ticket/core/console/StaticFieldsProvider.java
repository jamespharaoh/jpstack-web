package wbs.services.ticket.core.console;

import lombok.NonNull;

import wbs.console.forms.FormFieldSet;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;

@PrototypeComponent ("staticFieldsProvider")
public
class StaticFieldsProvider<
	ObjectType extends Record<ObjectType>,
	ParentType extends Record<ParentType>
>
	implements FieldsProvider<ObjectType,ParentType> {

	// state

	FormFieldSet formFields;

	// implementation

	@Override
	public
	FormFieldSet getFieldsForObject (
			@NonNull ObjectType object) {

		return getStaticFields ();

	}

	@Override
	public
	FormFieldSet getFieldsForParent (
			@NonNull ParentType parent) {

		return getStaticFields ();

	}

	@Override
	public
	FormFieldSet getStaticFields () {

		return formFields;

	}

	public
	StaticFieldsProvider<ObjectType,ParentType> setFields (
			@NonNull FormFieldSet fields) {

		formFields =
			fields;

		return this;

	}

}
