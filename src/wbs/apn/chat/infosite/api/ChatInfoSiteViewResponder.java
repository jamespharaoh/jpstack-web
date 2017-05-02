package wbs.apn.chat.infosite.api;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteHtml;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPost;
import static wbs.web.utils.HtmlUtils.htmlLinkWriteHtml;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.help.model.ChatHelpTemplateObjectHelper;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.apn.chat.infosite.model.ChatInfoSiteObjectHelper;
import wbs.apn.chat.infosite.model.ChatInfoSiteRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.PrintResponder;

@PrototypeComponent ("chatInfoSiteViewResponder")
public
class ChatInfoSiteViewResponder
	extends PrintResponder {

	// singleton dependencies

	@SingletonDependency
	ChatHelpTemplateObjectHelper chatHelpTemplateHelper;

	@SingletonDependency
	ChatInfoSiteObjectHelper chatInfoSiteHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// state

	ChatInfoSiteRec infoSite;
	ChatHelpTemplateRec infoSiteHelpTemplate;

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

			infoSite =
				chatInfoSiteHelper.findRequired (
					transaction,
					requestContext.requestIntegerRequired (
						"chatInfoSiteId"));

			if (
				stringNotEqualSafe (
					infoSite.getToken (),
					requestContext.requestStringRequired (
						"chatInfoSiteToken"))
			) {

				throw new RuntimeException (
					"Token mismatch");

			}

			infoSiteHelpTemplate =
				chatHelpTemplateHelper.findByTypeAndCode (
					transaction,
					infoSite.getChatUser ().getChat (),
					"system",
					"info_site_help");

		}

	}

	@Override
	protected
	void goHeaders (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goHeaders");

		) {

			requestContext.addHeader (
				"Content-Type",
				"text/html");

		}

	}

	@Override
	protected
	void goContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goContent");

		) {

			formatWriter.writeLineFormat (
				"<!DOCTYPE html>");

			formatWriter.writeLineFormatIncreaseIndent (
				"<html>");

			goHead (
				transaction);

			goBody (
				transaction);

			formatWriter.writeLineFormatDecreaseIndent (
				"</html>");

		}

	}

	protected
	void goHead (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goHead");

		) {

			formatWriter.writeLineFormatIncreaseIndent (
				"<head>");

			formatWriter.writeLineFormat (
				"<title>%h</title>",
				"User profiles");

			formatWriter.writeLineFormatDecreaseIndent (
				"</head>");

		}

	}

	protected
	void goBody (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goBody");

		) {

			formatWriter.writeLineFormatIncreaseIndent (
				"<body>");

			for (
				int index = 0;
				index < infoSite.getOtherChatUsers ().size ();
				index ++
			) {

				int currentIndex = index;

				ChatUserRec otherUser =
					infoSite.getOtherChatUsers ().get (
						index);

				if (index > 0) {

					formatWriter.writeLineFormat (
						"<hr>");

				}

				htmlParagraphWriteHtml (
					() -> htmlLinkWriteHtml (
						stringFormat (
							"%u/%u/full",
							infoSite.getToken (),
							integerToDecimalString (
								currentIndex)),
						() -> formatWriter.writeLineFormat (
							"<img src=\"%h\">",
							stringFormat (
								"%u/%u/normal",
								infoSite.getToken (),
								integerToDecimalString (
									currentIndex)))));

				htmlParagraphWriteFormat (
					"User: %h",
					otherUser.getCode ());

				if (otherUser.getName () != null) {

					htmlParagraphWriteFormat (
						"Name: %h",
						otherUser.getName ());

				}

				if (otherUser.getInfoText () != null) {

					htmlParagraphWriteFormat (
						"Info: %h",
						otherUser.getInfoText ().getText ());

				}

				htmlFormOpenPost ();

				formatWriter.writeLineFormat (
					"<input",
					" type=\"hidden\"",
					" name=\"otherUserId\"",
					" value=\"%h\"",
					integerToDecimalString (
						otherUser.getId ()),
					">\n");

				htmlParagraphOpen ();

				formatWriter.writeLineFormat (
					"<input",
					" type=\"text\"",
					" name=\"text\"",
					">");

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" value=\"send\"",
					">");

				htmlParagraphClose ();

				htmlFormClose ();

			}

			formatWriter.writeLineFormat (
				"%s",
				infoSiteHelpTemplate.getText ());

			formatWriter.writeLineFormatDecreaseIndent (
				"</body>");

		}

	}

}
