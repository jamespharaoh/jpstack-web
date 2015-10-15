package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Map;

import javax.inject.Inject;

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
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

import com.google.common.collect.ImmutableMap;

@PrototypeComponent ("chatUserAdminCreditAction")
public
class ChatUserAdminCreditAction
	extends ConsoleAction {

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserCreditObjectHelper chatUserCreditHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {
		return responder ("chatUserAdminCreditResponder");
	}

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
			paramsChecker.apply (requestContext);

		if (params == null)
			return null;

		// get params

		int creditAmount =
			(Integer) params.get ("creditAmount");

		int billAmount =
			(Integer) params.get ("billAmount");

		String details =
			(String) params.get ("details");

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		ChatUserCreditRec chatUserCredit =
			chatUserCreditHelper.insert (
				new ChatUserCreditRec ()

			.setChatUser (
				chatUser)

			.setTimestamp (
				instantToDate (
					transaction.now ()))

			.setCreditAmount (
				creditAmount)

			.setBillAmount (
				billAmount)

			.setUser (
				myUser)

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
