package wbs.applications.imchat.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.joinWithSpace;
import static wbs.framework.utils.etc.Misc.optionalIf;
import static wbs.framework.utils.etc.Misc.presentInstances;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.NonNull;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatMessageObjectHelper;
import wbs.applications.imchat.model.ImChatMessageRec;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.PrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("imChatPendingSummaryPart")
public
class ImChatPendingSummaryPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject @Named
	ConsoleModule imChatPendingConsoleModule;

	@Inject
	ImChatMessageObjectHelper imChatMessageHelper;

	@Inject
	PrivChecker privChecker;

	// state

	FormFieldSet customerFields;
	FormFieldSet messageFields;

	ImChatMessageRec imChatMessage;
	ImChatConversationRec imChatConversation;

	// implementation

	@Override
	public
	void prepare () {

		// get field sets

		customerFields =
			imChatPendingConsoleModule.formFieldSets ().get (
				"customerFields");

		messageFields =
			imChatPendingConsoleModule.formFieldSets ().get (
				"messageFields");

		// load data

		imChatMessage =
			imChatMessageHelper.find (
				requestContext.stuffInt (
					"imChatMessageId"));

		imChatConversation =
			imChatMessage.getImChatConversation ();

	}

	@Override
	public
	void renderHtmlBodyContent () {

		goSummary ();

		goHistory ();

	}

	void goHistory () {

		printFormat (
			"<h3>Conversation history</h3>\n");

		// retrieve messages

		List<ImChatMessageRec> messages =
			new ArrayList<ImChatMessageRec> (
				imChatConversation.getMessagesIn ());

		List<ImChatMessageRec> reverseMessages =
			Lists.reverse (
				messages);

		// create message table

		printFormat (
			"<table class=\"list\">\n");

		// header

		printFormat (
			"<tr>\n");

		formFieldLogic.outputTableHeadings (
			formatWriter,
			messageFields);

		printFormat (
			"</tr>\n");

		// row

		for (
			ImChatMessageRec message
				: reverseMessages
		) {

			if (
				isNotNull (
					message.getPartnerImChatMessage ())
			) {

				ImChatMessageRec partnerMessage =
					message.getPartnerImChatMessage ();

				printFormat (
					"<tr",
					" class=\"%h\"",
					classForMessage (
						partnerMessage),
					">\n");

				formFieldLogic.outputTableCellsList (
					formatWriter,
					messageFields,
					partnerMessage,
					true);

				printFormat (
					"</tr>\n");

			}

			printFormat (
				"<tr",
				" class=\"%h\"",
				joinWithSpace (
					presentInstances (
						Optional.of (
							classForMessage (
								message)),
						optionalIf (
							equal (
								message,
								imChatMessage),
							"selected"))),
				">\n");

			formFieldLogic.outputTableCellsList (
				formatWriter,
				messageFields,
				message,
				true);

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"<tr>\n");

		printFormat (
			"</table>\n");

	}

	void goSummary () {

		ImChatCustomerRec imChatCustomer =
			imChatConversation.getImChatCustomer ();

		printFormat (
			"<h3>Customer summary</h3>\n");

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputTableRows (
			formatWriter,
			customerFields,
			imChatCustomer);

		printFormat (
			"</table>\n");

	}

	String classForMessage (
			@NonNull ImChatMessageRec message) {

		if (
			isNotNull (
				message.getPrice ())
		) {

			return "message-out-charge";

		} else if (
			isNotNull (
				message.getSenderUser ())
		) {

			return "message-out";

		} else {

			return "message-in";

		}

	}

}
