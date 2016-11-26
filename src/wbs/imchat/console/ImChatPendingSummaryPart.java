package wbs.imchat.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIf;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlStyleAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlDivClose;
import static wbs.web.utils.HtmlBlockUtils.htmlDivOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingThreeWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteHtml;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryEditableScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerDetailTypeRec;
import wbs.imchat.model.ImChatCustomerDetailValueRec;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatMessageObjectHelper;
import wbs.imchat.model.ImChatMessageRec;
import wbs.imchat.model.ImChatProfileRec;
import wbs.imchat.model.ImChatRec;
import wbs.web.utils.HtmlUtils;

@PrototypeComponent ("imChatPendingSummaryPart")
public
class ImChatPendingSummaryPart
	extends AbstractPagePart {

	// dependencies

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@ClassSingletonDependency
	LogContext logContext;

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

	FormFieldSet <ImChatCustomerRec> customerFields;
	FormFieldSet <ImChatProfileRec> profileFields;
	FormFieldSet <ImChatMessageRec> messageFields;

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
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		// get field sets

		customerFields =
			imChatPendingConsoleModule.formFieldSet (
				"customerFields",
				ImChatCustomerRec.class);

		profileFields =
			imChatPendingConsoleModule.formFieldSet (
				"profileFields",
				ImChatProfileRec.class);

		messageFields =
			imChatPendingConsoleModule.formFieldSet (
				"messageFields",
				ImChatMessageRec.class);

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
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

		htmlDivOpen (
			htmlClassAttribute (
				"layout-container"));

		htmlTableOpen (
			htmlClassAttribute (
				"layout"));

		htmlTableRowOpen ();

		htmlTableCellOpen (
			htmlStyleAttribute (
				htmlStyleRuleEntry (
					"width",
					"50%")));

		goCustomerSummary (
			taskLogger);

		goCustomerDetails ();

		htmlTableCellClose ();

		htmlTableCellOpen (
			htmlStyleAttribute (
				htmlStyleRuleEntry (
					"width",
					"50%")));

		goProfileSummary (
			taskLogger);

		goCustomerNotes ();

		htmlTableCellClose ();

		htmlTableRowClose ();

		htmlTableClose ();

		htmlDivClose ();

		goHistory (
			taskLogger);

	}

	void goCustomerDetails () {

		htmlHeadingThreeWrite (
			"Customer details");

		htmlTableOpenDetails ();

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

			htmlTableDetailsRowWrite (
				detailType.getName (),
				ifNotNullThenElseEmDash (
					detailValue,
					() -> detailValue.getValue ()));

		}

		htmlTableClose ();

	}

	void goCustomerSummary (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goCustomerSummary");

		htmlHeadingThreeWrite (
			"Customer summary");

		formFieldLogic.outputDetailsTable (
			taskLogger,
			formatWriter,
			customerFields,
			customer,
			ImmutableMap.of ());

	}

	void goProfileSummary (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goProfileSummary");

		htmlHeadingThreeWrite (
			"Profile summary");

		formFieldLogic.outputDetailsTable (
			taskLogger,
			formatWriter,
			profileFields,
			profile,
			ImmutableMap.of ());

	}

	void goCustomerNotes () {

		htmlHeadingThreeWrite (
			"Notes");

		htmlParagraphWriteHtml (
			HtmlUtils.newlineToBr (
				HtmlUtils.htmlEncode (
					customer.getNotesText () != null
						? customer.getNotesText ().getText ()
						: "")),
			htmlIdAttribute (
				stringFormat (
					"im-chat-customer-note-%s",
					integerToDecimalString (
						customer.getId ()))),
			htmlClassAttribute (
				"im-chat-customer-note-editable"));

	}

	void goHistory (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goHistory");

		htmlHeadingThreeWrite (
			"Conversation history");

		// retrieve messages

		List <ImChatMessageRec> messages =
			new ArrayList<> (
				conversation.getMessagesIn ());

		List <ImChatMessageRec> historyRequests =
			Lists.reverse (
				messages);

		// create message table

		htmlTableOpenList ();

		// header

		htmlTableRowOpen ();

		formFieldLogic.outputTableHeadings (
			formatWriter,
			messageFields);

		htmlTableRowClose ();

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

				htmlTableRowOpen (
					htmlClassAttribute (
						classForMessage (
							historyReply)));

				formFieldLogic.outputTableCellsList (
					taskLogger,
					formatWriter,
					messageFields,
					historyReply,
					emptyMap (),
					true);

				htmlTableRowClose ();

			}

			htmlTableRowOpen (
				htmlClassAttribute (
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

			)));

			formFieldLogic.outputTableCellsList (
				taskLogger,
				formatWriter,
				messageFields,
				historyRequest,
				emptyMap (),
				true);

			htmlTableRowClose ();

		}

		htmlTableClose ();

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
