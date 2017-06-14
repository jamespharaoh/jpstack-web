package wbs.apn.chat.user.image.console;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.NonNull;

import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.io.RuntimeIoException;

import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.web.responder.BufferedResponder;

@PrototypeComponent ("chatUserImageZipResponder")
public
class ChatUserImageZipResponder
	extends BufferedResponder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	List <ChatUserRec> chatUsers;

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

			chatUsers =
				genericCastUnchecked (
					requestContext.request (
						"chatUsers"));

		}

	}

	@Override
	public
	void headers (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"headers");

		) {

			requestContext.contentType (
				"application/zip");

			requestContext.setHeader (
				"Content-Disposition",
				"download; filename=photos.zip");

		}

	}

	@Override
	public
	void render (
			@NonNull Transaction parentTransaction,
			@NonNull OutputStream outputStream) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"render");

			ZipOutputStream zipOutputStream =
				new ZipOutputStream (
					outputStream);

		) {

			zipOutputStream.setMethod (
				ZipOutputStream.STORED);

			for (
				ChatUserRec chatUser
					: chatUsers
			) {

				ZipEntry zipEntry =
					new ZipEntry (
						chatUser.getCode () + ".jpg");

				ChatUserImageRec chatUserImage =
					chatUser.getChatUserImageList ().get (0);

				byte[] data =
					chatUserImage.getMedia ().getContent ().getData ();

				zipEntry.setSize (
					data.length);

				zipEntry.setCrc (
					getCrc32 (data));

				zipEntry.setTime (
					chatUserImage.getTimestamp ().getMillis ());

				zipOutputStream.putNextEntry (
					zipEntry);

				zipOutputStream.write (
					data);

				zipOutputStream.closeEntry ();

			}

			zipOutputStream.finish ();
			zipOutputStream.close ();

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	private static
	long getCrc32 (
			byte[] data) {

		CRC32 crc32 =
			new CRC32 ();

		crc32.update (
			data);

		return crc32.getValue ();

	}

}
