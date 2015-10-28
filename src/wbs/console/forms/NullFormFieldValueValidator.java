package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.doNothing;

import java.util.List;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("nullFormFieldValueValidator")
public
class NullFormFieldValueValidator<Generic>
	implements FormFieldValueValidator<Generic> {

	@Override
	public
	void validate (
			@NonNull Optional<Generic> genericValue,
			@NonNull List<String> errors) {

		doNothing ();

	}

}
