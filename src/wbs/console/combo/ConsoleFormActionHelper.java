package wbs.console.combo;

import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;

import com.google.common.base.Optional;

public
interface ConsoleFormActionHelper<FormState> {

	FormState constructFormState ();

	void updatePassiveFormState (
			FormState formState);

	Optional<Responder> processFormSubmission (
			Transaction transaction,
			FormState formState);

}
