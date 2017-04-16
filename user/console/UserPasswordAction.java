package wbs.platform.user.console;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lombok.NonNull;

import org.apache.commons.codec.binary.Base64;

import wbs.console.action.ConsoleAction;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.model.UserRec;

import wbs.web.responder.Responder;

@PrototypeComponent ("userPasswordAction")
public
class UserPasswordAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("userPasswordResponder");
	}

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		String password1 =
			requestContext.parameterRequired (
				"password_1");

		String password2 =
			requestContext.parameterRequired (
				"password_2");

		try (

			Transaction transaction =
				database.beginReadWrite (
					"UserPasswordAction.goReal ()",
					this);

		) {

			// load user

			UserRec user =
				userHelper.findFromContextRequired ();

			// check privs

			if (

				referenceNotEqualWithClass (
					UserRec.class,
					user,
					userConsoleLogic.userRequired ())

				&& ! privChecker.canRecursive (
					taskLogger,
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
				taskLogger,
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

}
