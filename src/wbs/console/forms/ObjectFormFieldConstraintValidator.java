package wbs.console.forms;

import lombok.NonNull;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("objectFormFieldConstraintValidator")
public
class ObjectFormFieldConstraintValidator<Container,Generic>
	implements FormFieldConstraintValidator<Container,Generic> {

	@Override
	public
	Optional<String> validate (
			@NonNull Container container,
			@NonNull Optional<Generic> nativeValue) {

		return Optional.<String>absent ();

	}

}
