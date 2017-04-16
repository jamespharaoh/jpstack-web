package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.param.FixedParamChecker;
import wbs.console.param.ParamChecker;
import wbs.console.param.ParamCheckerSet;
import wbs.console.param.RegexpParamChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.apn.chat.bill.model.ChatUserCreditObjectHelper;
import wbs.apn.chat.bill.model.ChatUserCreditRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.Responder;

@PrototypeComponent ("chatUserAdminCreditAction")
public
class ChatUserAdminCreditAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserCreditObjectHelper chatUserCreditHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"chatUserAdminCreditResponder");

	}

	// implementation

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goReal");

		// check privs

		if (! requestContext.canContext ("chat.userCredit")) {

			requestContext.addError ("Access denied");

			return null;

		}

		// check params

		Map<String,Object> params =
			paramsChecker.apply (
				requestContext);

		if (params == null)
			return null;

		// get params

		long creditAmount =
			(Long)
			params.get (
				"creditAmount");

		long billAmount =
			(Long)
			params.get (
				"billAmount");

		String details =
			(String)
			params.get (
				"details");

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"ChatUserAdminCreditAction.goReal ()",
					this);

		) {

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired ();

			ChatUserCreditRec chatUserCredit =
				chatUserCreditHelper.insert (
					taskLogger,
					chatUserCreditHelper.createInstance ()

				.setChatUser (
					chatUser)

				.setTimestamp (
					transaction.now ())

				.setCreditAmount (
					creditAmount)

				.setBillAmount (
					billAmount)

				.setUser (
					userConsoleLogic.userRequired ())

				.setGift (
					billAmount == 0)

				.setDetails (
					details)

			);

			chatUser

				.setCredit (
					+ chatUser.getCredit ()
					+ creditAmount)

				.setCreditBought (
					+ chatUser.getCreditBought ()
					+ creditAmount);

			transaction.commit ();

			requestContext.setEmptyFormData ();

			requestContext.addNoticeFormat (
				"Credit adjusted, reference = %h",
				integerToDecimalString (
					chatUserCredit.getId ()));

			return null;

		}

	}

	final static
	ParamCheckerSet paramsChecker =
		new ParamCheckerSet (
			new ImmutableMap.Builder<String,ParamChecker<?>> ()

		.put (
			"creditAmount",
			new FixedParamChecker (
				"Invalid credit amount",
				true,
				2))

		.put (
			"billAmount",
			new FixedParamChecker (
				"Invalid bill amount",
				true,
				2))

		.put (
			"details",
			new RegexpParamChecker (
				"Please enter details",
				".+"))

		.build ()

	);

}
