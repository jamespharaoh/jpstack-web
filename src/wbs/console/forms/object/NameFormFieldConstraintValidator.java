package wbs.console.forms.object;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.types.FormFieldConstraintValidator;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

@PrototypeComponent ("nameFormFieldConstraintValidator")
public
class NameFormFieldConstraintValidator <Container>
	implements FormFieldConstraintValidator <Container, String> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Optional <String> validate (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <String> nativeValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"validate");

		) {

			/*

			TODO make this work

			if (codeChanged) {

				Record<?> existing =
					consoleHelper.findByCode (
						objectManager.getParentGlobalId (
							(Record<?>) object),
						newCode);

				if (existing != null) {

					requestContext.addError ("Name already in use");

					throw new InvalidFormValueException ();

				}

			}
			*/

			return optionalAbsent ();

		}

	}

}
