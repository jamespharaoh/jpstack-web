package wbs.console.forms;

import java.util.List;

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
	void validate (
			@NonNull Container container,
			@NonNull Optional<Generic> nativeValue,
			@NonNull List<String> errors) {

		// TODO validate the root and permissions

	}

}
