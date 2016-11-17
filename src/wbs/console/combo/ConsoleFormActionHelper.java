package wbs.console.combo;

import java.util.Map;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

import wbs.web.responder.Responder;

public
interface ConsoleFormActionHelper <FormState> {

	Boolean canBePerformed ();

	Map <String, String> placeholderValues ();

	FormState constructFormState ();

	void updatePassiveFormState (
			FormState formState);

	Optional <Responder> processFormSubmission (
			Transaction transaction,
			FormState formState);

}
