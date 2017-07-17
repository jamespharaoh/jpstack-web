package wbs.console.forms.core;

import com.google.common.collect.ImmutableList;

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
@PrototypeComponent ("dynamicFieldsProvider")
public
class DynamicFieldsProvider <ObjectType, ParentType>
	implements FieldsProvider <ObjectType, ParentType> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	Class <ObjectType> containerClass;

	@Getter @Setter
	Class <ParentType> parentClass;

	@Getter @Setter
	FieldsProvider <ObjectType, ParentType> staticFieldsProvider;

	@Getter @Setter
	FieldsProvider <ObjectType, ParentType> dynamicFieldsProvider;

	// implementation

	@Override
	public
	FormFieldSetPair <ObjectType> getFieldsForObject (
			@NonNull Transaction parentTransaction,
			@NonNull ObjectType object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getFieldsForObject");

		) {

			FormFieldSetPair <ObjectType> staticFields =
				staticFieldsProvider.getFieldsForObject (
					transaction,
					object);

			FormFieldSetPair <ObjectType> dynamicFields =
				dynamicFieldsProvider.getFieldsForObject (
					transaction,
					object);

			return new FormFieldSetPair <ObjectType> ()

				.columnFields (
					new CombinedFormFieldSet <ObjectType> (
						name,
						containerClass,
						ImmutableList.of (
							staticFields.columnFields (),
							dynamicFields.columnFields ())))

				.rowFields (
					new CombinedFormFieldSet <ObjectType> (
						name,
						containerClass,
						ImmutableList.of (
							staticFields.rowFields (),
							dynamicFields.rowFields ())))

			;

		}

	}

	@Override
	public
	FormFieldSetPair <ObjectType> getFieldsForParent (
			@NonNull Transaction parentTransaction,
			@NonNull ParentType parent) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getFieldsForParent");

		) {

			FormFieldSetPair <ObjectType> staticFields =
				staticFieldsProvider.getFieldsForParent (
					transaction,
					parent);

			FormFieldSetPair <ObjectType> dynamicFields =
				dynamicFieldsProvider.getFieldsForParent (
					transaction,
					parent);

			return new FormFieldSetPair <ObjectType> ()

				.columnFields (
					new CombinedFormFieldSet <ObjectType> (
						name,
						containerClass,
						ImmutableList.of (
							staticFields.columnFields (),
							dynamicFields.columnFields ())))

				.rowFields (
					new CombinedFormFieldSet <ObjectType> (
						name,
						containerClass,
						ImmutableList.of (
							staticFields.rowFields (),
							dynamicFields.rowFields ())))

			;

		}

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

			FormFieldSetPair <ObjectType> staticFields =
				staticFieldsProvider.getStaticFields (
					transaction);

			FormFieldSetPair <ObjectType> dynamicFields =
				dynamicFieldsProvider.getStaticFields (
					transaction);

			return new FormFieldSetPair <ObjectType> ()

				.columnFields (
					new CombinedFormFieldSet <ObjectType> (
						name,
						containerClass,
						ImmutableList.of (
							staticFields.columnFields (),
							dynamicFields.columnFields ())))

				.rowFields (
					new CombinedFormFieldSet <ObjectType> (
						name,
						containerClass,
						ImmutableList.of (
							staticFields.rowFields (),
							dynamicFields.rowFields ())))

			;

		}

	}

}
