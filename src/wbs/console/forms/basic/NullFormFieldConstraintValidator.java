package wbs.console.forms.basic;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.types.FormFieldConstraintValidator;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.Transaction;

@PrototypeComponent ("nullFormFieldConstraintValidator")
public
class NullFormFieldConstraintValidator <Container, Native>
	implements FormFieldConstraintValidator <Container, Native> {

	// transaction

	@Override
	public
	Optional <String> validate (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Native> nativeValue) {

		return optionalAbsent ();

	}

}
