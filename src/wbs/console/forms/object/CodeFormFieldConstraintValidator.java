package wbs.console.forms.object;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.types.FormFieldConstraintValidator;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.Transaction;

@PrototypeComponent ("codeFormFieldConstraintValidator")
public
class CodeFormFieldConstraintValidator <Container>
	implements FormFieldConstraintValidator <Container, String> {

	// transaction

	@Override
	public
	Optional <String> validate (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <String> nativeValue) {

		return optionalAbsent ();

	}

}
