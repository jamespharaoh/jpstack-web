package wbs.clients.apn.chat.help.console;

import java.util.List;

import javax.inject.Inject;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.help.model.ChatHelpLogRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.TimeFormatter;
import wbs.framework.utils.etc.Html;
import wbs.platform.service.console.ServiceConsoleHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageSearch;
import wbs.sms.message.core.model.MessageSearch.MessageSearchOrder;
import wbs.sms.number.core.model.NumberRec;

@PrototypeComponent ("chatHelpLogPendingMessagesPart")
public
class ChatHelpLogPendingMessagesPart
	extends AbstractPagePart {

	@Inject
	ChatHelpLogConsoleHelper chatHelpLogHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ServiceConsoleHelper serviceHelper;

	@Inject
	TimeFormatter timeFormatter;

	// state

	ChatHelpLogRec chatHelpLog;
	ChatUserRec chatUser;
	ChatRec chat;

	List<MessageRec> messages;

	// implementation

	@Override
	public
	void prepare () {

		chatHelpLog =
			chatHelpLogHelper.findRequired (
				requestContext.stuffInteger (
					"chatHelpLogId"));

		chatUser =
			chatHelpLog.getChatUser ();

		chat =
			chatUser.getChat ();

		ServiceRec service =
			serviceHelper.findByCodeRequired (
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
			messageHelper.search(
				messageSearch);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"list\">");

		printFormat (
			"<tr>\n",
			"<th>From</th>\n",
			"<th>To</th>\n",
			"<th>Timestamp</th>\n",
			"<th>Charge</th>\n",
			"</tr>\n");

		for (MessageRec message
				: messages) {

			printFormat (
				"<tr class=\"sep\">\n");

			String rowClass;

			if (message.getDirection () == MessageDirection.in) {

				rowClass = "message-in";

			} else if (message.getCharge () > 0) {

				rowClass = "message-out-charge";

			} else {

				rowClass = "message-out";

			}

			printFormat (
				"<tr class=\"%h\">\n",
				rowClass,

				"<td>%h</td>\n",
				message.getNumFrom (),

				"<td>%h</td>\n",
				message.getNumTo (),

				"<td>%h</td>\n",
				timeFormatter.timestampTimezoneString (
					chatUserLogic.getTimezone (
						chatUser),
					message.getCreatedTime ()),

				"<td>%h</td>\n",
				message.getCharge (),

				"</tr>\n");

			printFormat (
				"<tr class=\"%h\">\n",
				rowClass,

				"<td colspan=\"4\">%s</td>\n",
				Html.encodeNewlineToBr (
					message.getText ().getText ()),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
