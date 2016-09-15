package wbs.imchat.console;

import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalIf;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import lombok.NonNull;
import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerDetailTypeRec;
import wbs.imchat.model.ImChatCustomerDetailValueRec;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatMessageObjectHelper;
import wbs.imchat.model.ImChatMessageRec;
import wbs.imchat.model.ImChatProfileRec;
import wbs.imchat.model.ImChatRec;
import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryEditableScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.utils.web.HtmlUtils;

@PrototypeComponent ("imChatPendingSummaryPart")
public
class ImChatPendingSummaryPart
	extends AbstractPagePart {

	// dependencies

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	@Named
	ConsoleModule imChatPendingConsoleModule;

	@SingletonDependency
	ImChatMessageObjectHelper imChatMessageHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	// state

	FormFieldSet customerFields;
	FormFieldSet profileFields;
	FormFieldSet messageFields;

	ImChatMessageRec message;
	ImChatConversationRec conversation;
	ImChatCustomerRec customer;
	ImChatProfileRec profile;
	ImChatRec imChat;

	boolean canSupervise;

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				JqueryEditableScriptRef.instance)

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/im-chat.js"))

			.build ();

	}

	@Override
	public
	Set<HtmlLink> links () {

		return ImmutableSet.<HtmlLink>builder ()

			.addAll (
				super.links ())

			.add (
				HtmlLink.applicationCssStyle (
					"/styles/im-chat.css"))

			.build ();

	}

	// implementation

	@Override
	public
	void prepare () {

		// get field sets

		customerFields =
			imChatPendingConsoleModule.formFieldSets ().get (
				"customerFields");

		profileFields =
			imChatPendingConsoleModule.formFieldSets ().get (
				"profileFields");

		messageFields =
			imChatPendingConsoleModule.formFieldSets ().get (
				"messageFields");

		// load data

		message =
			imChatMessageHelper.findRequired (
				requestContext.stuffInteger (
					"imChatMessageId"));

		conversation =
			message.getImChatConversation ();

		customer =
			conversation.getImChatCustomer ();

		profile =
			conversation.getImChatProfile ();

		imChat =
			customer.getImChat ();

		// misc

		canSupervise =
			privChecker.canRecursive (
				imChat,
				"supervisor");

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<div class=\"layout-container\">\n",
			"<table class=\"layout\">\n",
			"<tbody>\n",
			"<tr>\n",
			"<td style=\"width: 50%%\">\n");

		goCustomerSummary ();
		goCustomerDetails ();

		printFormat (
			"</td>\n",
			"<td style=\"width: 50%%\">\n");

		goProfileSummary ();
		goCustomerNotes ();

		printFormat (
			"</td>\n",
			"</tr>\n",
			"</tbody>\n",
			"</table>\n",
			"</div>\n");

		goHistory ();

	}

	void goCustomerDetails () {

		printFormat (
			"<h3>Customer details</h3>\n");

		printFormat (
			"<table class=\"details\">\n",
			"<tbody>\n");

		for (
			ImChatCustomerDetailTypeRec detailType
				: imChat.getCustomerDetailTypes ()
		) {

			if (

				detailType.getRestricted ()

				&& ! canSupervise

			) {
				continue;
			}

			ImChatCustomerDetailValueRec detailValue =
				customer.getDetails ().get (
					detailType.getId ());

			printFormat (
				"<tr>\n",
				"<th>%h</th>\n",
				detailType.getName (),
				"<td>%h</td>\n",
				detailValue != null
					? detailValue.getValue ()
					: "-",
				"</tr>\n");

		}

		printFormat (
			"</tbody>\n",
			"</table>\n");

	}

	void goCustomerSummary () {

		printFormat (
			"<h3>Customer summary</h3>\n");

		formFieldLogic.outputDetailsTable (
			formatWriter,
			customerFields,
			customer,
			ImmutableMap.of ());

	}

	void goProfileSummary () {

		printFormat (
			"<h3>Profile summary</h3>\n");

		formFieldLogic.outputDetailsTable (
			formatWriter,
			profileFields,
			profile,
			ImmutableMap.of ());

	}

	void goCustomerNotes () {

		printFormat (
			"<h3>Notes</h3>\n");

		printFormat (
			"<p",
			" id=\"%h\"",
			stringFormat (
				"im-chat-customer-note-%d",
				customer.getId ()),
			" class=\"im-chat-customer-note-editable\"",
			">%s</p>\n",
			HtmlUtils.newlineToBr (
				HtmlUtils.htmlEncode (
					customer.getNotesText () != null
						? customer.getNotesText ().getText ()
						: "")));

	}

	void goHistory () {

		printFormat (
			"<h3>Conversation history</h3>\n");

		// retrieve messages

		List<ImChatMessageRec> messages =
			new ArrayList<ImChatMessageRec> (
				conversation.getMessagesIn ());

		List<ImChatMessageRec> historyRequests =
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
			ImChatMessageRec historyRequest
				: historyRequests
		) {

			if (
				isNotNull (
					historyRequest.getPartnerImChatMessage ())
			) {

				ImChatMessageRec historyReply =
					historyRequest.getPartnerImChatMessage ();

				printFormat (
					"<tr",
					" class=\"%h\"",
					classForMessage (
						historyReply),
					">\n");

				formFieldLogic.outputTableCellsList (
					formatWriter,
					messageFields,
					historyReply,
					ImmutableMap.of (),
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
							historyRequest)),

					optionalIf (
						referenceEqualWithClass (
							ImChatMessageRec.class,
							message,
							historyRequest),
						() -> "selected")

				)),
				">\n");

			formFieldLogic.outputTableCellsList (
				formatWriter,
				messageFields,
				historyRequest,
				ImmutableMap.of (),
				true);

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"<tr>\n");

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
