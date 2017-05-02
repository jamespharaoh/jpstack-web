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
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"canBePerformed");

		) {

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			boolean canView =
				userPrivChecker.canRecursive (
					transaction,
					chatUser.getChat (),
					"user_credit");

			boolean canPerform = (

				enumEqualSafe (
					chatUser.getType (),
					ChatUserType.user)

				&& ! chatUserLogic.deleted (
					transaction,
					chatUser)

			);

			return new Permissions ()
				.canView (canView)
				.canPerform (canPerform);

		}

	}

	@Override
	public
	ChatUserAdminCreditForm constructFormState (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"constructFormState");

		) {

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			return new ChatUserAdminCreditForm ()

				.currentCredit (
					chatUser.getCredit ());

		}

	}

	@Override
	public
	Map <String, Object> formHints (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"formHints");

		) {

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			return ImmutableMap.of (
				"chat",
				chatUser.getChat ());

		}

	}

	@Override
	public
	void writePreamble (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Boolean submit) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"writePreamble");

		) {

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

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
					transaction,
					chatUser)
			) {

				htmlParagraphWriteFormat (
					formatWriter,
					"This user has been deleted and can not be credit.");

			} else {

				htmlParagraphWriteFormat (
					formatWriter,
					"The credit amount is the actual credit to give the user. The ",
					"bill amount is the amount they have paid.");

				htmlParagraphWriteFormat (
					formatWriter,
					"To give someone some free credit enter it in credit amount ",
					"and enter 0.00 in bill amount. To process a credit card ",
					"payment enter the amount of credit in credit amount and the ",
					"amount they have paid in bill amount.");

				htmlParagraphWriteFormat (
					formatWriter,
					"You may also use negative amounts to adjust a previous error ",
					"as appropriate.");

			}

		}

	}

	@Override
	public
	Optional <Responder> processFormSubmission (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserAdminCreditForm formState) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"processFormSubmission");

		) {

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			ChatUserCreditRec chatUserCredit =
				chatUserCreditHelper.insert (
					transaction,
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
					userConsoleLogic.userRequired (
						transaction))

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

	}

	@Override
	public
	List <ChatUserCreditRec> history (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"history");

		) {

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			return ImmutableList.copyOf (
				chatUser.getChatUserCredits ());

		}

	}

}
