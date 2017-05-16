package wbs.console.forms.core;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FieldsProvider;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

@Accessors (fluent = true)
@PrototypeComponent ("staticFieldsProvider")
public
class StaticFieldsProvider <ObjectType, ParentType>
	implements FieldsProvider <ObjectType, ParentType> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	Class <ObjectType> containerClass;

	@Getter @Setter
	Class <ParentType> parentClass;

	@Getter @Setter
	FormFieldSet <ObjectType> columnFields;

	@Getter @Setter
	FormFieldSet <ObjectType> rowFields;

	// implementation

	@Override
	public
	FormFieldSetPair <ObjectType> getFieldsForObject (
			@NonNull Transaction parentTransaction,
			@NonNull ObjectType object) {

		return getStaticFields (
			parentTransaction);

	}

	@Override
	public
	FormFieldSetPair <ObjectType> getFieldsForParent (
			@NonNull Transaction parentTransaction,
			@NonNull ParentType parent) {

		return getStaticFields (
			parentTransaction);

	}

	@Override
	public
	FormFieldSetPair <ObjectType> getStaticFields (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getStaticFields");

		) {

			return new FormFieldSetPair <ObjectType> ()

				.columnFields (
					columnFields)

				.rowFields (
					rowFields)

			;

		}

	}

}
