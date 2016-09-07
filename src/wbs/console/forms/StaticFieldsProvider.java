package wbs.console.forms;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.record.Record;

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
