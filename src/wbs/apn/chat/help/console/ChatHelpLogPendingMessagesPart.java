package wbs.apn.chat.help.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowSeparatorWrite;
import static wbs.web.utils.HtmlUtils.htmlEncodeNewlineToBr;

import java.util.List;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.service.console.ServiceConsoleHelper;
import wbs.platform.service.model.ServiceRec;

import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageSearch;
import wbs.sms.message.core.model.MessageSearch.MessageSearchOrder;
import wbs.sms.number.core.model.NumberRec;

import wbs.utils.string.FormatWriter;
import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;

@PrototypeComponent ("chatHelpLogPendingMessagesPart")
public
class ChatHelpLogPendingMessagesPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatHelpLogConsoleHelper chatHelpLogHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	ServiceConsoleHelper serviceHelper;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatHelpLogRec chatHelpLog;
	ChatUserRec chatUser;
	ChatRec chat;

	List <MessageRec> messages;

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			chatHelpLog =
				chatHelpLogHelper.findFromContextRequired (
					transaction);

			chatUser =
				chatHelpLog.getChatUser ();

			chat =
				chatUser.getChat ();

			ServiceRec service =
				serviceHelper.findByCodeRequired (
					transaction,
					chat,
					"default");

			NumberRec number =
				chatHelpLog.getChatUser ().getNumber ();

			MessageSearch messageSearch =
				new MessageSearch ()

				.serviceId (
					service.getId ())

				.numberId (
					number.getId ())

				.orderBy (
					MessageSearchOrder.createdTime);

			messages =
				messageHelper.search (
					transaction,
					messageSearch);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlTableOpenList (
				formatWriter);

			htmlTableHeaderRowWrite (
				formatWriter,
				"From",
				"To",
				"Timestamp",
				"Charge");

			for (
				MessageRec message
					: messages
			) {

				htmlTableRowSeparatorWrite (
					formatWriter);

				String rowClass;

				if (message.getDirection () == MessageDirection.in) {
					rowClass = "message-in";
				} else if (message.getCharge () > 0) {
					rowClass = "message-out-charge";
				} else {
					rowClass = "message-out";
				}

				// message attributes row

				htmlTableRowOpen (
					formatWriter,
					htmlClassAttribute (
						rowClass));

				htmlTableCellWrite (
					formatWriter,
					message.getNumFrom ());

				htmlTableCellWrite (
					formatWriter,
					message.getNumTo ());

				htmlTableCellWrite (
					formatWriter,
					timeFormatter.timestampTimezoneString (
						chatUserLogic.getTimezone (
							chatUser),
						message.getCreatedTime ()));

				htmlTableCellWrite (
					formatWriter,
					integerToDecimalString (
						message.getCharge ()));

				htmlTableRowClose (
					formatWriter);

				// message content row

				htmlTableRowOpen (
					formatWriter,
					htmlClassAttribute (
						rowClass));

				htmlTableCellWrite (
					formatWriter,
					htmlEncodeNewlineToBr (
						message.getText ().getText ()),
					htmlColumnSpanAttribute (
						4l));

				htmlTableRowClose (
					formatWriter);

			}

			htmlTableClose (
				formatWriter);

		}

	}

}
