package wbs.platform.core.console;

import static wbs.utils.collection.CollectionUtils.collectionHasTwoElements;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listSecondElementRequired;
import static wbs.utils.etc.DebugUtils.debugFormat;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringInSafe;
import static wbs.utils.string.StringUtils.stringSplitFullStop;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.console.UserSessionLogic;
import wbs.platform.user.model.UserRec;
import wbs.platform.user.model.UserSessionRec;

import wbs.web.responder.Responder;

@PrototypeComponent ("coreLogonAction")
public
class CoreLogonAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserSessionLogic userSessionLogic;

	@SingletonDependency
	WbsConfig wbsConfig;

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
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goReal");

		// get params

		@NonNull
		String slice =
			requestContext.parameterRequired (
				"slice");

		@NonNull
		String username =
			requestContext.parameterRequired (
				"username");

		@NonNull
		String password =
			requestContext.parameterRequired (
				"password");

		// extract slice from username if present

		if (
			optionalIsPresent (
				requestContext.header (
					"x-wbs-slice"))
		) {

			List <String> usernameParts =
				stringSplitFullStop (
					username);

			if (
				collectionHasTwoElements (
					usernameParts)
			) {

				slice =
					listFirstElementRequired (
						usernameParts);

				username =
					listSecondElementRequired (
						usernameParts);

			}

		}

		// check we got the right params

		if (
			stringInSafe (
				"",
				slice,
				username,
				password)
		) {
			return null;
		}

		// attempt login

		Optional <Long> userIdOptional;

		try (

			Transaction transaction =
				database.beginReadWrite (
					"CoreLogonAction.goReal ()",
					this);

		) {

			// attempt logon

debugFormat (
	"Attempting logon");

			Optional <UserSessionRec> userSessionOptional =
				userSessionLogic.userLogonTry (
					taskLogger,
					slice.toLowerCase (),
					username.toLowerCase (),
					password,
					requestContext.header (
						"User-Agent"),
					requestContext.cookie (
						"txt2_console"));

			if (
				optionalIsPresent (
					userSessionOptional)
			) {

debugFormat (
	"Logon success");

				UserSessionRec userSession =
					optionalGetRequired (
						userSessionOptional);

				UserRec user =
					userSession.getUser ();

				userIdOptional =
					optionalOf (
						user.getId ());

			} else {

debugFormat (
	"Logon failure");

				userIdOptional =
					optionalAbsent ();

			}

			transaction.commit ();

		}

		// if it failed show the logon page again

		if (
			optionalIsNotPresent (
				userIdOptional)
		) {

			requestContext.addWarningFormat (
				"Sorry, the details you entered did not match. Please try ",
				"again, or contact an appropriate person for help.");

			taskLogger.warningFormat (
				"Failed logon attempt for %s.%s",
				slice,
				username);

			return null;

		}

		Long userId =
			optionalGetRequired (
				userIdOptional);

		// save the userid

		taskLogger.noticeFormat (
			"Successful logon for %s.%s (%s)",
			slice,
			username,
			integerToDecimalString (
				userId));

		// and redirect to the console proper

		return responder (
			"coreRedirectResponder");

	}

}
