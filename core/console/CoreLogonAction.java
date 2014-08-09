package wbs.platform.core.console;

import static wbs.framework.utils.etc.Misc.disallowNulls;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.service.model.ServiceObjectHelper;
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
	UserLogic userLogic;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("coreLogonResponder");
	}

	// implementation

	@Override
	protected
	Responder goReal () {

		// get params

		String slice =
			requestContext.parameter ("slice");

		String username =
			requestContext.parameter ("username");

		String password =
			requestContext.parameter ("password");

		// check we got the right params

		disallowNulls (
			slice,
			username,
			password);

		if (in ("",
				slice,
				username,
				password))
			return null;

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		// attempt logon

		Integer userId =
			userLogic.userLogonTry (
				slice.toLowerCase (),
				username.toLowerCase (),
				password,
				requestContext.sessionId ());

		transaction.commit ();

		// if it failed show the logon page again

		if (userId == null) {

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

		requestContext.session (
			"myUserId",
			userId);

		// and redirect to the console proper

		return responder ("coreRedirectResponder");

	}

}
