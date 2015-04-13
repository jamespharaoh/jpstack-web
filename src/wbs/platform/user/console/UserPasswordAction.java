package wbs.platform.user.console;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;

import lombok.Cleanup;

import org.apache.commons.codec.binary.Base64;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("userPasswordAction")
public
class UserPasswordAction
	extends ConsoleAction {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	PrivChecker privChecker;

	@Inject
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {
		return responder ("userPasswordResponder");
	}

	@Override
	public
	Responder goReal () {

		String password1 =
			requestContext.parameter ("password_1");

		String password2 =
			requestContext.parameter ("password_2");

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		// load user

		UserRec user =
			userHelper.find (
				requestContext.stuffInt ("userId"));

		// check privs

		if (user.getId () != requestContext.userId ()
			&& ! privChecker.can (user, "manage")) {

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

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		eventLogic.createEvent (
			"user_password_reset",
			myUser,
			user);

		transaction.commit ();

		// return

		requestContext.addNotice (
			"Password updated");

		return null;

	}

}
