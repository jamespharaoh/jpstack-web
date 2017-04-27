package wbs.apn.chat.user.image.console;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.io.IOException;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.NonNull;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.io.RuntimeIoException;

import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageRec;

@PrototypeComponent ("chatUserImageZipResponder")
public
class ChatUserImageZipResponder
	extends ConsoleResponder {

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
			@NonNull TaskLogger parentTaskLogger) {

		chatUsers =
			genericCastUnchecked (
				requestContext.request (
					"chatUsers"));

	}

	@Override
	public
	void setHtmlHeaders (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setHtmlHeaders");

		) {

			requestContext.setHeader (
				"Content-Type",
				"application/zip");

			requestContext.setHeader (
				"Content-Disposition",
				"download; filename=photos.zip");

		}

	}

	@Override
	public
	void render (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			ZipOutputStream zipOutputStream =
				new ZipOutputStream (
					requestContext.outputStream ());

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
