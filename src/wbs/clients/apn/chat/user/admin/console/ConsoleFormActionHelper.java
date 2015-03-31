package wbs.clients.apn.chat.user.admin.console;

import wbs.framework.web.Responder;

import com.google.common.base.Optional;

public
interface ConsoleFormActionHelper<FormState> {

	FormState constructFormState ();

	void updatePassiveFormState (
			FormState formState);

	Optional<Responder> processFormSubmission (
			FormState formState);

}
