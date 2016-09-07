package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;

import wbs.clients.apn.chat.bill.model.ChatUserCreditObjectHelper;
import wbs.clients.apn.chat.bill.model.ChatUserCreditRec;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.param.FixedParamChecker;
import wbs.console.param.ParamChecker;
import wbs.console.param.ParamCheckerSet;
import wbs.console.param.RegexpParamChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

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

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			"chatUserAdminCreditResponder");

	}

	// implementation

	@Override
	public
	Responder goReal () {

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

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserAdminCreditAction.goReal ()",
				this);

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

		ChatUserCreditRec chatUserCredit =
			chatUserCreditHelper.insert (
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

		requestContext.addNotice (
			stringFormat (
				"Credit adjusted, reference = %h",
				chatUserCredit.getId ()));

		return null;

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
