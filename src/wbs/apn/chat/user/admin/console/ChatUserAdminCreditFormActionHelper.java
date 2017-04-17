package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.console.formaction.AbstractConsoleFormActionHelper;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleLogic;

import wbs.utils.string.FormatWriter;

import wbs.apn.chat.bill.console.ChatUserCreditConsoleHelper;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.bill.model.ChatUserCreditRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.web.responder.Responder;

@SingletonComponent ("chatUserAdminCreditFormActionHelper")
public
class ChatUserAdminCreditFormActionHelper
	extends AbstractConsoleFormActionHelper <
		ChatUserAdminCreditForm,
		ChatUserCreditRec
	> {

	// singleton dependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserCreditConsoleHelper chatUserCreditHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserPrivChecker userPrivChecker;

	// implementation

	@Override
	public
	Permissions canBePerformed (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"canBePerformed");

		ChatUserRec chatUser =
			chatUserHelper.findFromContextRequired ();

		boolean canView =
			userPrivChecker.canRecursive (
				taskLogger,
				chatUser.getChat (),
				"user_credit");

		boolean canPerform = (

			enumEqualSafe (
				chatUser.getType (),
				ChatUserType.user)

			&& ! chatUserLogic.deleted (
				chatUser)

		);

		return new Permissions ()
			.canView (canView)
			.canPerform (canPerform);

	}

	@Override
	public
	ChatUserAdminCreditForm constructFormState () {

		ChatUserRec chatUser =
			chatUserHelper.findFromContextRequired ();

		return new ChatUserAdminCreditForm ()

			.currentCredit (
				chatUser.getCredit ());

	}

	@Override
	public
	Map <String, Object> formHints () {

		ChatUserRec chatUser =
			chatUserHelper.findFromContextRequired ();

		return ImmutableMap.<String, Object> builder ()

			.put (
				"chat",
				chatUser.getChat ())

			.build ();

	}

	@Override
	public
	void writePreamble (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull Boolean submit) {

		ChatUserRec chatUser =
			chatUserHelper.findFromContextRequired ();

		if (
			enumEqualSafe (
				chatUser.getType (),
				ChatUserType.monitor)
		) {

			htmlParagraphWriteFormat (
				formatWriter,
				"This is a monitor and can not be credited.");

		} else if (
			chatUserLogic.deleted (
				chatUser)
		) {

			htmlParagraphWriteFormat (
				formatWriter,
				"This user has been deleted and can not be credit.");

		} else {

			htmlParagraphWriteFormat (
				formatWriter,
				"Please note: The credit amount is the actual credit to give ",
				"the user. The bill amount is the amount they have paid. To ",
				"give someone some free credit enter it in credit amount and ",
				"enter 0.00 in bill amount. To process a credit card payment ",
				"enter the amount of credit in credit amount and the amount ",
				"they have paid in bill amount.");

		}

	}

	@Override
	public
	Optional <Responder> processFormSubmission (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction,
			@NonNull ChatUserAdminCreditForm formState) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"processFormSubmission");

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
				formState.creditAmount ())

			.setBillAmount (
				formState.billAmount ())

			.setUser (
				userConsoleLogic.userRequired ())

			.setGift (
				equalToZero (
					formState.billAmount ()))

			.setDetails (
				formState.details ())

		);

		chatUser

			.setCredit (
				+ chatUser.getCredit ()
				+ formState.creditAmount ())

			.setCreditBought (
				+ chatUser.getCreditBought ()
				+ formState.creditAmount ());

		transaction.commit ();

		requestContext.setEmptyFormData ();

		requestContext.addNoticeFormat (
			"Credit adjusted, reference = %h",
			integerToDecimalString (
				chatUserCredit.getId ()));

		return optionalAbsent ();

	}

	@Override
	public
	List <ChatUserCreditRec> history () {

		ChatUserRec chatUser =
			chatUserHelper.findFromContextRequired ();

		return ImmutableList.copyOf (
			chatUser.getChatUserCredits ());

	}

}
