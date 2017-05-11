package wbs.console.forms.types;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

public
interface FormFieldConstraintValidator <Container, Native> {

	Optional <String> validate (
			Transaction parentTransaction,
			Container container,
			Optional <Native> nativeValue);

}
