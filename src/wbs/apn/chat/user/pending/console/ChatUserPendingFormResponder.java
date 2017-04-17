package wbs.apn.chat.user.pending.console;

import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlStyleAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingOneWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlInputUtils.htmlOptionWrite;
import static wbs.web.utils.HtmlInputUtils.htmlSelectClose;
import static wbs.web.utils.HtmlInputUtils.htmlSelectOpen;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleHtmlResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.console.MediaConsoleLogic;

import wbs.apn.chat.help.console.ChatHelpTemplateConsoleHelper;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.logic.ChatUserLogic.PendingMode;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.apn.chat.user.image.model.ChatUserImageType;

@PrototypeComponent ("chatUserPendingFormResponder")
public
class ChatUserPendingFormResponder
	extends ConsoleHtmlResponder {

	// singleton dependencies

	@SingletonDependency
	ChatHelpTemplateConsoleHelper chatHelpTemplateHelper;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	ChatUserRec chatUser;

	List <ChatHelpTemplateRec> chatHelpTemplates =
		Collections.emptyList ();

	PendingMode mode;

	// details

	@Override
	protected
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/chat-user-pending-form.js"))

			.build ();

	}

	// implementation

	@Override
	protected
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		chatUser =
			chatUserHelper.findFromContextRequired ();

		if (
			isNotNull (
				chatUser.getNewChatUserName ())
		) {

			mode =
				PendingMode.name;

		} else if (
			isNotNull (
				chatUser.getNewChatUserInfo ())
		) {

			mode =
				PendingMode.info;

		} else if (
			isNotNull (
				chatUserLogic.chatUserPendingImage (
					chatUser,
					ChatUserImageType.image))
		) {

			mode =
				PendingMode.image;

		} else if (
			isNotNull (
				chatUserLogic.chatUserPendingImage (
					chatUser,
					ChatUserImageType.video))
		) {

			mode =
				PendingMode.video;

		} else if (
			isNotNull (
				chatUserLogic.chatUserPendingImage (
					chatUser,
					ChatUserImageType.audio))
		) {

			mode =
				PendingMode.audio;

		} else {

			mode =
				PendingMode.none;

		}

		if (mode.rejectType () != null) {

			chatHelpTemplates =
				chatHelpTemplateHelper.findByParentAndType (
					chatUser.getChat (),
					mode.rejectType ());

		}

	}

	@Override
	public
	void renderHtmlHeadContents (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlHeadContents");

		super.renderHtmlHeadContents (
			taskLogger);

		renderScriptBlock ();

	}

	private
	void renderScriptBlock () {

		htmlScriptBlockOpen ();

		formatWriter.writeLineFormat (
			"var chatHelpTemplates = new Array ();");

		for (
			ChatHelpTemplateRec chatHelpTemplate
				: chatHelpTemplates
		) {

			formatWriter.writeLineFormat (
				"chatHelpTemplates [%s] = '%j';",
				integerToDecimalString (
					chatHelpTemplate.getId ()),
				chatHelpTemplate.getText ());

		}

		formatWriter.writeLineFormat (
			"top.show_inbox (true);");

		formatWriter.writeLineFormat (
			"top.frames ['main'].location = '%j';",
			requestContext.resolveApplicationUrlFormat (
				"/chatUser.pending",
				"/%u",
				integerToDecimalString (
					chatUser.getId ()),
				"/chatUser.pending.summary"));

		htmlScriptBlockClose ();

	}

	@Override
	protected
	void renderHtmlBodyContents (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContents");

		htmlHeadingOneWrite (
			"Chat user—approve info");

		requestContext.flushNotices (
			formatWriter);

		// form open

		htmlFormOpenPostAction (
			requestContext.resolveApplicationUrlFormat (
				"/chatUser.pending",
				"/%u",
				integerToDecimalString (
					chatUser.getId ()),
				"/chatUser.pending.form"));

		if (mode == PendingMode.none) {

			htmlParagraphWrite (
				"No info to approve");

			if (
				privChecker.canRecursive (
					taskLogger,
					GlobalId.root,
					"manage")
			) {

				htmlParagraphOpen ();

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"chatUserDismiss\"",
					" value=\"dismiss queue item\"",
					">");

				htmlParagraphClose ();

			}

		} else {

			htmlTableOpenDetails ();

			htmlTableDetailsRowWrite (
				"User",
				stringFormat (
					"%s/%s",
					chatUser.getChat ().getCode (),
					chatUser.getCode ()));

			htmlTableDetailsRowWriteHtml (
				"Options",
				() -> {

				formatWriter.writeLineFormat (
					"<input",
					" type=\"button\"",
					" value=\"approve\"",
					" onclick=\"showPhoto ()\"",
					">");

				formatWriter.writeLineFormat (
					"<input",
					" type=\"button\"",
					" value=\"reject\"",
					" onclick=\"showReject ()\"",
					">");

			});

			switch (mode) {

			case name:

				htmlTableDetailsRowWriteHtml (
					"Name",
					() -> formatWriter.writeLineFormat (
						"<textarea",
						" name=\"name\"",
						" rows=\"4\"",
						" cols=\"48\"",
						">%h</textarea>",
						chatUser
							.getNewChatUserName ()
							.getOriginalName ()),
					htmlIdAttribute (
						"photoRow"));

				break;

			case info:

				htmlTableDetailsRowWriteHtml (
					"Info",
					() -> formatWriter.writeLineFormat (
						"<textarea",
						" name=\"info\"",
						" rows=\"4\"",
						" cols=\"48\"",
						">%h</textarea>",
						chatUser
							.getNewChatUserInfo ()
							.getOriginalText ()
							.getText ()),
					htmlIdAttribute (
						"photoRow"));

				break;

			case image:

				ChatUserImageRec image =
					chatUserLogic.chatUserPendingImage (
						chatUser,
						ChatUserImageType.image);

				htmlTableRowOpen (
					htmlIdAttribute (
						"photoRow"));

				htmlTableHeaderCellWrite (
					"Photo");

				htmlTableCellOpen ();

				mediaConsoleLogic.writeMediaThumb100 (
					taskLogger,
					image.getMedia ());

				htmlTableCellClose ();

				htmlTableRowClose ();

				break;

			case video:

				ChatUserImageRec video =
					chatUserLogic.chatUserPendingImage (
						chatUser,
						ChatUserImageType.video);

				htmlTableRowOpen (
					htmlIdAttribute (
						"photoRow"));

				htmlTableHeaderRowWrite (
					"Video");

				htmlTableCellOpen ();

				mediaConsoleLogic.writeMediaThumb100 (
					taskLogger,
					video.getMedia ());

				htmlTableCellClose ();

				htmlTableRowClose ();

				break;

			case audio:

				htmlTableRowOpen (
					htmlIdAttribute (
						"photoRow"));

				htmlTableHeaderRowWrite (
					"Audio");

				htmlTableCellWrite (
					"(audio)");

				htmlTableRowClose ();

				break;

			default:

				doNothing ();

			}

			// media

			if (
				enumInSafe (
					mode,
					PendingMode.image,
					PendingMode.video,
					PendingMode.audio)
			) {

				ChatUserImageRec chatUserImage =
					chatUserLogic.chatUserPendingImage (
						chatUser,
						chatUserLogic.imageTypeForMode (mode));

				htmlTableRowOpen (
					htmlIdAttribute (
						"classificationRow"));

				htmlTableHeaderCellWrite (
					"Classification");

				htmlTableCellOpen ();

				htmlSelectOpen (
					"classification");

				htmlOptionWrite (
					"primary",
					requestContext.formOrEmptyString (
						"classification"),
					"primary");

				if (

					chatUserImage.getAppend ()

					|| stringEqualSafe (
						chatUser.getChat ().getCode (),
						"adult")

				) {

					htmlOptionWrite (
						"secondary",
						requestContext.formOrEmptyString (
							"classification"),
						"secondary");

				}

				if (
					chatUserImage.getAppend ()
				) {

					htmlOptionWrite (
						"landscape",
						requestContext.formOrEmptyString (
							"classification"),
						"landscape");

				}

				htmlSelectClose ();

				htmlTableCellClose ();

				htmlTableRowClose ();

			}

			// template

			htmlTableRowOpen (
				htmlIdAttribute (
					"templateRow"),
				htmlStyleAttribute (
					htmlStyleRuleEntry (
						"display",
						"none")));

			htmlTableHeaderCellWrite (
				"Template");

			htmlTableCellOpen ();

			htmlSelectOpen (
				htmlIdAttribute (
					"templateId"));

			htmlOptionWrite (
				"");

			for (
				ChatHelpTemplateRec chatelpTemplate
					: chatHelpTemplates
			) {

				htmlOptionWrite (
					integerToDecimalString (
						chatelpTemplate.getId ()),
					chatelpTemplate.getCode ());

			}

			htmlSelectClose ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"button\"",
				" onclick=\"useTemplate ()\"",
				" value=\"ok\"",
				">");

			htmlTableCellClose ();

			htmlTableRowClose ();

			// message

			htmlTableRowOpen (
				htmlIdAttribute (
					"messageRow"),
				htmlStyleAttribute (
					htmlStyleRuleEntry (
						"display",
						"none")));

			htmlTableHeaderCellWrite (
				"Message");

			htmlTableCellOpen ();

			formatWriter.writeLineFormat (
				"<textarea",
				" id=\"message\"",
				" name=\"message\"",
				" rows=\"4\"",
				" cols=\"48\"",
				"></textarea>");

			htmlTableCellClose ();

			htmlTableRowClose ();

			// actions

			htmlTableRowOpen ();

			htmlTableHeaderCellWrite (
				"Actions");

			htmlTableCellOpen ();

			switch (mode) {

			case name:

				formatWriter.writeLineFormat (
					"<input",
					" id=\"approveButton\"",
					" type=\"submit\"",
					" name=\"chatUserNameApprove\"",
					" value=\"approve name\"",
					">");

				formatWriter.writeLineFormat (
					"<input",
					" id=\"rejectButton\"",
					" style=\"display: none\"",
					" type=\"submit\"",
					" name=\"chatUserNameReject\"",
					" value=\"reject name and send warning\"",
					">");

				break;

			case info:

				formatWriter.writeLineFormat (
					"<input",
					" id=\"approveButton\"",
					" type=\"submit\"",
					" name=\"chatUserInfoApprove\"",
					" value=\"approve info\"",
					">");

				formatWriter.writeLineFormat (
					"<input",
					" id=\"rejectButton\"",
					" style=\"display: none\"",
					" type=\"submit\"",
					" name=\"chatUserInfoReject\"",
					" value=\"reject info and send warning\"",
					">");

				break;

			case image:

				formatWriter.writeLineFormat (
					"<input",
					" id=\"approveButton\"",
					" type=\"submit\"",
					" name=\"chatUserImageApprove\"",
					" value=\"approve photo\"",
					">");

				formatWriter.writeLineFormat (
					"<input",
					" id=\"rejectButton\"",
					" style=\"display: none\"",
					" type=\"submit\"",
					" name=\"chatUserImageReject\"",
					" value=\"reject photo and send warning\"",
					">");

				break;

			case video:

				formatWriter.writeLineFormat (
					"<input",
					" id=\"approveButton\"",
					" type=\"submit\"",
					" name=\"chatUserVideoApprove\"",
					" value=\"approve video\"",
					">");

				formatWriter.writeLineFormat (
					"<input",
					" id=\"rejectButton\"",
					" style=\"display: none\"",
					" type=\"submit\"",
					" name=\"chatUserVideoReject\"",
					" value=\"reject video and send warning\"",
					">");

				break;

			case audio:

				formatWriter.writeLineFormat (
					"<input",
					" id=\"approveButton\"",
					" type=\"submit\"",
					" name=\"chatUserAudioApprove\"",
					" value=\"approve audio\"",
					">");

				formatWriter.writeLineFormat (
					"<input",
					" id=\"rejectButton\"",
					" style=\"display: none\"",
					" type=\"submit\"",
					" name=\"chatUserAudioReject\"",
					" value=\"reject audio and send warning\"",
					">");

				break;

			default:

				// do nothing

			}

			htmlTableCellClose ();

			htmlTableRowClose ();

			htmlTableClose ();

		}

		// form close

		htmlFormClose ();

	}

}