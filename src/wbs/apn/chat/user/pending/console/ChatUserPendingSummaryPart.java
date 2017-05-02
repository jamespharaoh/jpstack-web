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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlTableOpenDetails ();

			htmlTableHeaderRowWrite (
				"",
				"Old",
				"New");

			htmlTableDetailsRowWriteRaw (
				"User",
				() -> consoleObjectManager.writeTdForObjectMiniLink (
					transaction,
					chatUser,
					2l));

			// name

			htmlTableDetailsRowWriteRaw (
				"Name",
				() -> htmlTableCellWrite (
					ifNullThenEmDash (
						chatUser.getName ()),
					htmlColumnSpanAttribute (2l)));

			// info

			htmlTableDetailsRowWriteRaw (
				"Info",
				() -> ifNotNullThenElse (
					chatUser.getNewChatUserInfo (),

				() -> {

					htmlTableCellWrite (
						ifNotNullThenElseEmDash (
							chatUser.getInfoText (),
							() -> chatUser.getInfoText ().getText ()));

					htmlTableCellWrite (
						chatUser.getNewChatUserInfo ().getOriginalText ()
							.getText ());

				},

				() -> htmlTableCellWrite (
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
				"Photo",
				() -> ifNotNullThenElse (
					newImage,

				() -> {

					htmlTableCellWrite (
						existingImageHtml);

					htmlTableCellWriteHtml (
						() -> mediaConsoleLogic.writeMediaContent (
							transaction,
							formatWriter,
							newImage.getMedia ()));
				},

				() -> htmlTableCellWriteHtml (
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
					chatUser.getChatUserImageList ().get (0).getMedia ()),

				() -> formatWriter.writeFormat (
					"—")

			);

			htmlTableDetailsRowWriteRaw (
				"Video",
				() -> ifNotNullThenElse (
					newVideo,

				() -> {

					htmlTableCellWrite (
						existingVideoHtml);

					htmlTableCellWriteHtml (
						() -> mediaConsoleLogic.writeMediaContent (
							transaction,
							newVideo.getMedia ()));

				},

				() -> htmlTableCellWriteHtml (
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
					chatUser.getChatUserAudioList ().get (0).getMedia ()),

				() -> formatWriter.writeFormat (
					"—")

			);

			htmlTableDetailsRowWriteRaw (
				"Audio",
				() -> ifNotNullThenElse (
					newAudio,

				() -> {

					htmlTableCellWrite (
						existingAudioHtml);

					htmlTableCellWriteHtml (
						() -> mediaConsoleLogic.writeMediaContent (
							transaction,
							newAudio.getMedia ()));

				},

				() -> htmlTableCellWriteHtml (
					existingAudioHtml,
					htmlColumnSpanAttribute (2l))

			));

			// close table

			htmlTableClose ();

		}

	}

}
