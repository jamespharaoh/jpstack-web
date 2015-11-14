package wbs.console.combo;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;

public
interface ConsoleFormActionHelper<FormState> {

	FormState constructFormState ();

	void updatePassiveFormState (
			FormState formState);

	Optional<Responder> processFormSubmission (
			Transaction transaction,
			FormState formState);

}
