package wbs.platform.core.console;

import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.logic.UserLogic;

@Log4j
@PrototypeComponent ("coreLogonAction")
public
class CoreLogonAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserLogic userLogic;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			"coreLogonResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal () {

		// get params

		@NonNull
		String slice =
			requestContext.parameterOrNull ("slice");

		@NonNull
		String username =
			requestContext.parameterOrNull ("username");

		@NonNull
		String password =
			requestContext.parameterOrNull ("password");

		// check we got the right params

		if (in ("",
				slice,
				username,
				password))
			return null;

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		// attempt logon

		Long userId =
			userLogic.userLogonTry (
				slice.toLowerCase (),
				username.toLowerCase (),
				password,
				requestContext.sessionId (),
				requestContext.header (
					"User-Agent"));

		transaction.commit ();

		// if it failed show the logon page again

		if (userId == null) {

			requestContext.addWarning (
				"Sorry, the details you entered did not match. Please try " +
				"again, or contact an appropriate person for help.");

			log.warn (
				stringFormat (
					"Failed logon attempt for %s.%s",
					slice,
					username));

			return null;

		}

		// save the userid

		log.info (
			stringFormat (
				"Successful logon for %s.%s (%s)",
				slice,
				username,
				userId));

		userConsoleLogic.login (
			userId);

		// and redirect to the console proper

		return responder (
			"coreRedirectResponder");

	}

}
