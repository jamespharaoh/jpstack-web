package wbs.platform.user.console;

import static wbs.framework.utils.etc.Misc.notEqual;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;

import lombok.Cleanup;

import org.apache.commons.codec.binary.Base64;

import wbs.console.action.ConsoleAction;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("userPasswordAction")
public
class UserPasswordAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	UserPrivChecker privChecker;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("userPasswordResponder");
	}

	@Override
	public
	Responder goReal () {

		String password1 =
			requestContext.parameterOrNull ("password_1");

		String password2 =
			requestContext.parameterOrNull ("password_2");

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		// load user

		UserRec user =
			userHelper.find (
				requestContext.stuffInt (
					"userId"));

		// check privs

		if (

			notEqual (
				user,
				userConsoleLogic.userRequired ())

			&& ! privChecker.canRecursive (
				user,
				"manage")

		) {

			requestContext.addError (
				"Access denied");

			return null;

		}

		// check for password

		if (password1.length () == 0
			&& password2.length () == 0) {

			requestContext.addError (
				"No password supplied");

			return null;

		}

		// check passwords match

		if (! password1.equals (password2)) {

			requestContext.addError (
				"Passwords do not match");

			return null;

		}

		// digest password

		String hash;

		try {

			MessageDigest messageDigest =
				MessageDigest.getInstance ("SHA-1");

			messageDigest.update (
				password1.getBytes ());

			hash =
				Base64.encodeBase64String (
					messageDigest.digest ());

		} catch (NoSuchAlgorithmException exception) {

			requestContext.addError (
				"Internal error");

			return null;

		}

		// update user

		user.setPassword (hash);

		// create an event

		eventLogic.createEvent (
			"user_password_reset",
			userConsoleLogic.userRequired (),
			user);

		transaction.commit ();

		// return

		requestContext.addNotice (
			"Password updated");

		return null;

	}

}
