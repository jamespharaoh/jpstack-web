package wbs.apn.chat.user.pending.console;

import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.NullUtils.isNotNull;
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
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;

import wbs.platform.media.console.MediaConsoleLogic;

import wbs.utils.string.FormatWriter;

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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

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
						transaction,
						chatUser,
						ChatUserImageType.image))
			) {

				mode =
					PendingMode.image;

			} else if (
				isNotNull (
					chatUserLogic.chatUserPendingImage (
						transaction,
						chatUser,
						ChatUserImageType.video))
			) {

				mode =
					PendingMode.video;

			} else if (
				isNotNull (
					chatUserLogic.chatUserPendingImage (
						transaction,
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
						transaction,
						chatUser.getChat (),
						mode.rejectType ());

			}

		}

	}

	@Override
	public
	void renderHtmlHeadContents (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlHeadContents");

		) {

			super.renderHtmlHeadContents (
				transaction,
				formatWriter);

			renderScriptBlock (
				transaction,
				formatWriter);

		}

	}

	private
	void renderScriptBlock (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderScriptBlock");

		) {

			htmlScriptBlockOpen (
				formatWriter);

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

			htmlScriptBlockClose (
				formatWriter);

		}

	}

	@Override
	protected
	void renderHtmlBodyContents (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContents");

		) {

			htmlHeadingOneWrite (
				formatWriter,
				"Chat user—approve info");

			requestContext.flushNotices (
				formatWriter);

			// form open

			htmlFormOpenPostAction (
				formatWriter,
				requestContext.resolveApplicationUrlFormat (
					"/chatUser.pending",
					"/%u",
					integerToDecimalString (
						chatUser.getId ()),
					"/chatUser.pending.form"));

			if (mode == PendingMode.none) {

				htmlParagraphWrite (
					formatWriter,
					"No info to approve");

				if (
					privChecker.canRecursive (
						transaction,
						GlobalId.root,
						"manage")
				) {

					htmlParagraphOpen (
						formatWriter);

					formatWriter.writeLineFormat (
						"<input",
						" type=\"submit\"",
						" name=\"chatUserDismiss\"",
						" value=\"dismiss queue item\"",
						">");

					htmlParagraphClose (
						formatWriter);

				}

			} else {

				htmlTableOpenDetails (
					formatWriter);

				htmlTableDetailsRowWrite (
					formatWriter,
					"User",
					stringFormat (
						"%s/%s",
						chatUser.getChat ().getCode (),
						chatUser.getCode ()));

				htmlTableDetailsRowWriteHtml (
					formatWriter,
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
						formatWriter,
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
						formatWriter,
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
							transaction,
							chatUser,
							ChatUserImageType.image);

					htmlTableRowOpen (
						formatWriter,
						htmlIdAttribute (
							"photoRow"));

					htmlTableHeaderCellWrite (
						formatWriter,
						"Photo");

					htmlTableCellOpen (
						formatWriter);

					mediaConsoleLogic.writeMediaThumb100 (
						transaction,
						formatWriter,
						image.getMedia ());

					htmlTableCellClose (
						formatWriter);

					htmlTableRowClose (
						formatWriter);

					break;

				case video:

					ChatUserImageRec video =
						chatUserLogic.chatUserPendingImage (
							transaction,
							chatUser,
							ChatUserImageType.video);

					htmlTableRowOpen (
						formatWriter,
						htmlIdAttribute (
							"photoRow"));

					htmlTableHeaderRowWrite (
						formatWriter,
						"Video");

					htmlTableCellOpen (
						formatWriter);

					mediaConsoleLogic.writeMediaThumb100 (
						transaction,
						formatWriter,
						video.getMedia ());

					htmlTableCellClose (
						formatWriter);

					htmlTableRowClose (
						formatWriter);

					break;

				case audio:

					htmlTableRowOpen (
						formatWriter,
						htmlIdAttribute (
							"photoRow"));

					htmlTableHeaderRowWrite (
						formatWriter,
						"Audio");

					htmlTableCellWrite (
						formatWriter,
						"(audio)");

					htmlTableRowClose (
						formatWriter);

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
							transaction,
							chatUser,
							chatUserLogic.imageTypeForMode (mode));

					htmlTableRowOpen (
						formatWriter,
						htmlIdAttribute (
							"classificationRow"));

					htmlTableHeaderCellWrite (
						formatWriter,
						"Classification");

					htmlTableCellOpen (
						formatWriter);

					htmlSelectOpen (
						formatWriter,
						"classification");

					htmlOptionWrite (
						formatWriter,
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
							formatWriter,
							"secondary",
							requestContext.formOrEmptyString (
								"classification"),
							"secondary");

					}

					if (
						chatUserImage.getAppend ()
					) {

						htmlOptionWrite (
							formatWriter,
							"landscape",
							requestContext.formOrEmptyString (
								"classification"),
							"landscape");

					}

					htmlSelectClose (
						formatWriter);

					htmlTableCellClose (
						formatWriter);

					htmlTableRowClose (
						formatWriter);

				}

				// template

				htmlTableRowOpen (
					formatWriter,
					htmlIdAttribute (
						"templateRow"),
					htmlStyleAttribute (
						htmlStyleRuleEntry (
							"display",
							"none")));

				htmlTableHeaderCellWrite (
					formatWriter,
					"Template");

				htmlTableCellOpen (
					formatWriter);

				htmlSelectOpen (
					formatWriter,
					htmlIdAttribute (
						"templateId"));

				htmlOptionWrite (
					formatWriter,
					"");

				for (
					ChatHelpTemplateRec chatelpTemplate
						: chatHelpTemplates
				) {

					htmlOptionWrite (
						formatWriter,
						integerToDecimalString (
							chatelpTemplate.getId ()),
						chatelpTemplate.getCode ());

				}

				htmlSelectClose (
					formatWriter);

				formatWriter.writeLineFormat (
					"<input",
					" type=\"button\"",
					" onclick=\"useTemplate ()\"",
					" value=\"ok\"",
					">");

				htmlTableCellClose (
					formatWriter);

				htmlTableRowClose (
					formatWriter);

				// message

				htmlTableRowOpen (
					formatWriter,
					htmlIdAttribute (
						"messageRow"),
					htmlStyleAttribute (
						htmlStyleRuleEntry (
							"display",
							"none")));

				htmlTableHeaderCellWrite (
					formatWriter,
					"Message");

				htmlTableCellOpen (
					formatWriter);

				formatWriter.writeLineFormat (
					"<textarea",
					" id=\"message\"",
					" name=\"message\"",
					" rows=\"4\"",
					" cols=\"48\"",
					"></textarea>");

				htmlTableCellClose (
					formatWriter);

				htmlTableRowClose (
					formatWriter);

				// actions

				htmlTableRowOpen (
					formatWriter);

				htmlTableHeaderCellWrite (
					formatWriter,
					"Actions");

				htmlTableCellOpen (
					formatWriter);

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

				htmlTableCellClose (
					formatWriter);

				htmlTableRowClose (
					formatWriter);

				htmlTableClose (
					formatWriter);

			}

			// form close

			htmlFormClose (
				formatWriter);

		}

	}

}