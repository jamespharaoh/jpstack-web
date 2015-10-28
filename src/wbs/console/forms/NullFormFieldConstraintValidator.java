package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.doNothing;

import java.util.List;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("nullFormFieldConstraintValidator")
public
class NullFormFieldConstraintValidator<Container,Native>
	implements FormFieldConstraintValidator<Container,Native> {

	@Override
	public
	void validate (
			@NonNull Container container,
			@NonNull Optional<Native> nativeValue,
			@NonNull List<String> errors) {

		doNothing ();

	}

}
