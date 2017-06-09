package wbs.apn.chat.user.pending.console;

import static wbs.utils.etc.LogicUtils.ifNotEmptyThenElse;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.LogicUtils.ifNullThenEmDash;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteRaw;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.media.console.MediaConsoleLogic;

import wbs.utils.string.FormatWriter;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.apn.chat.user.image.model.ChatUserImageType;

@PrototypeComponent ("chatUserPendingSummaryPart")
public
class ChatUserPendingSummaryPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	ConsoleObjectManager consoleObjectManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	// state

	ChatUserRec chatUser;

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

			chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

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

			htmlTableOpenDetails (
				formatWriter);

			htmlTableHeaderRowWrite (
				formatWriter,
				"",
				"Old",
				"New");

			htmlTableDetailsRowWriteRaw (
				formatWriter,
				"User",
				() -> consoleObjectManager.writeTdForObjectMiniLink (
					transaction,
					formatWriter,
					chatUser,
					2l));

			// name

			htmlTableDetailsRowWriteRaw (
				formatWriter,
				"Name",
				() -> htmlTableCellWrite (
					formatWriter,
					ifNullThenEmDash (
						chatUser.getName ()),
					htmlColumnSpanAttribute (2l)));

			// info

			htmlTableDetailsRowWriteRaw (
				formatWriter,
				"Info",
				() -> ifNotNullThenElse (
					chatUser.getNewChatUserInfo (),

				() -> {

					htmlTableCellWrite (
						formatWriter,
						ifNotNullThenElseEmDash (
							chatUser.getInfoText (),
							() -> chatUser.getInfoText ().getText ()));

					htmlTableCellWrite (
						formatWriter,
						chatUser.getNewChatUserInfo ().getOriginalText ()
							.getText ());

				},

				() -> htmlTableCellWrite (
					formatWriter,
					chatUser.getInfoText ().getText (),
					htmlColumnSpanAttribute (2l))

			));

			// photo

			ChatUserImageRec newImage =
				chatUserLogic.chatUserPendingImage (
					transaction,
					chatUser,
					ChatUserImageType.image);

			Runnable existingImageHtml = () ->
				ifNotEmptyThenElse (
					chatUser.getChatUserImageList (),

				() -> mediaConsoleLogic.writeMediaContent (
					transaction,
					formatWriter,
					chatUser.getChatUserImageList ().get (0).getMedia ()),

				() -> formatWriter.writeFormat (
					"—")

			);

			htmlTableDetailsRowWriteRaw (
				formatWriter,
				"Photo",
				() -> ifNotNullThenElse (
					newImage,

				() -> {

					htmlTableCellWrite (
						formatWriter,
						existingImageHtml);

					htmlTableCellWriteHtml (
						formatWriter,
						() -> mediaConsoleLogic.writeMediaContent (
							transaction,
							formatWriter,
							newImage.getMedia ()));
				},

				() -> htmlTableCellWriteHtml (
					formatWriter,
					existingImageHtml,
					htmlColumnSpanAttribute (2l))

			));

			// video

			ChatUserImageRec newVideo =
				chatUserLogic.chatUserPendingImage (
					transaction,
					chatUser,
					ChatUserImageType.video);

			Runnable existingVideoHtml = () ->
				ifNotEmptyThenElse (
					chatUser.getChatUserVideoList (),

				() -> mediaConsoleLogic.writeMediaContent (
					transaction,
					formatWriter,
					chatUser.getChatUserImageList ().get (0).getMedia ()),

				() -> formatWriter.writeFormat (
					"—")

			);

			htmlTableDetailsRowWriteRaw (
				formatWriter,
				"Video",
				() -> ifNotNullThenElse (
					newVideo,

				() -> {

					htmlTableCellWrite (
						formatWriter,
						existingVideoHtml);

					htmlTableCellWriteHtml (
						formatWriter,
						() -> mediaConsoleLogic.writeMediaContent (
							transaction,
							formatWriter,
							newVideo.getMedia ()));

				},

				() -> htmlTableCellWriteHtml (
					formatWriter,
					existingVideoHtml,
					htmlColumnSpanAttribute (2l))

			));

			// audio

			ChatUserImageRec newAudio =
				chatUserLogic.chatUserPendingImage (
					transaction,
					chatUser,
					ChatUserImageType.audio);

			Runnable existingAudioHtml = () ->
				ifNotEmptyThenElse (
					chatUser.getChatUserAudioList (),

				() -> mediaConsoleLogic.writeMediaContent (
					transaction,
					formatWriter,
					chatUser.getChatUserAudioList ().get (0).getMedia ()),

				() -> formatWriter.writeFormat (
					"—")

			);

			htmlTableDetailsRowWriteRaw (
				formatWriter,
				"Audio",
				() -> ifNotNullThenElse (
					newAudio,

				() -> {

					htmlTableCellWrite (
						formatWriter,
						existingAudioHtml);

					htmlTableCellWriteHtml (
						formatWriter,
						() -> mediaConsoleLogic.writeMediaContent (
							transaction,
							formatWriter,
							newAudio.getMedia ()));

				},

				() -> htmlTableCellWriteHtml (
					formatWriter,
					existingAudioHtml,
					htmlColumnSpanAttribute (2l))

			));

			// close table

			htmlTableClose (
				formatWriter);

		}

	}

}
