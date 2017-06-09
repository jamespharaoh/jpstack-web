package wbs.apn.chat.infosite.api;

import static wbs.utils.etc.IoUtils.writeBytes;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import java.io.OutputStream;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.media.model.MediaRec;

import wbs.apn.chat.infosite.model.ChatInfoSiteObjectHelper;
import wbs.apn.chat.infosite.model.ChatInfoSiteRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.BufferedResponder;

@PrototypeComponent ("chatInfoSiteImageResponder")
public
class ChatInfoSiteImageResponder
	extends BufferedResponder {

	// dependencies

	@SingletonDependency
	ChatInfoSiteObjectHelper chatInfoSiteHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// state

	ChatInfoSiteRec infoSite;
	MediaRec media;
	byte[] data;

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

			Long index =
				requestContext.requestIntegerRequired (
					"chatInfoSiteIndex");

			ChatUserRec chatUser =
				infoSite.getOtherChatUsers ().get (
					toJavaIntegerRequired (
						index));

			if (chatUser == null) {

				throw new RuntimeException (
					"Index out of bounds");

			}

			String mode =
				requestContext.requestStringRequired (
					"chatInfoSiteMode");

			if (
				stringEqualSafe (
					mode,
					"full")
			) {

				media =
					chatUser.getMainChatUserImage ().getFullMedia ();

			} else {

				media =
					chatUser.getMainChatUserImage ().getMedia ();

			}

			data =
				media.getContent ().getData ();

		}

	}

	@Override
	protected
	void headers (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goHeaders");

		) {

			requestContext.contentType (
				media.getMediaType ().getMimeType ());

			requestContext.setHeader (
				"Content-Length",
				Integer.toString (
					data.length));

		}

	}

	@Override
	protected
	void render (
			@NonNull Transaction parentTransaction,
			@NonNull OutputStream outputStream) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goContent");

		) {

			writeBytes (
				outputStream,
				data);

		}

	}

}
