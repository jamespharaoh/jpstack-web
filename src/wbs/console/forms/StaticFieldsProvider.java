package wbs.console.forms;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.record.Record;

@PrototypeComponent ("staticFieldsProvider")
public
class StaticFieldsProvider<
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
>
	implements FieldsProvider <ObjectType, ParentType> {

	// state

	FormFieldSet <ObjectType> fields;

	// implementation

	@Override
	public
	FormFieldSet <ObjectType> getFieldsForObject (
			@NonNull ObjectType object) {

		return getStaticFields ();

	}

	@Override
	public
	FormFieldSet <ObjectType> getFieldsForParent (
			@NonNull ParentType parent) {

		return getStaticFields ();

	}

	@Override
	public
	FormFieldSet <ObjectType> getStaticFields () {

		return fields;

	}

	public
	StaticFieldsProvider <ObjectType, ParentType> fields (
			@NonNull FormFieldSet <ObjectType> fields) {

		this.fields =
			fields;

		return this;

	}

}
