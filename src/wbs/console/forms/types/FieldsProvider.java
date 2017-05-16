package wbs.console.forms.types;

import static wbs.utils.etc.TypeUtils.dynamicCastRequired;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.forms.core.FormFieldSet;

import wbs.framework.database.Transaction;

public
interface FieldsProvider <Container, Parent> {

	Class <Container> containerClass ();

	Class <Parent> parentClass ();

	FormFieldSetPair <Container> getStaticFields (
			Transaction parentTransaction);

	FormFieldSetPair <Container> getFieldsForParent (
			Transaction parentTransaction,
			Parent parent);

	default
	FormFieldSetPair <Container> getFieldsForParentGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Object parent) {

		return getFieldsForParent (
			parentTransaction,
			dynamicCastRequired (
				parentClass (),
				parent));

	}

	FormFieldSetPair <Container> getFieldsForObject (
			Transaction parentTransaction,
			Container container);

	@Accessors (fluent = true)
	@Data
	public static
	class FormFieldSetPair <Container> {

		FormFieldSet <Container> columnFields;
		FormFieldSet <Container> rowFields;

	}

}
