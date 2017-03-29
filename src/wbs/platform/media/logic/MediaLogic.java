package wbs.platform.media.logic;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrThrow;
import static wbs.utils.string.StringUtils.stringFormat;

import java.awt.image.BufferedImage;
import java.util.Set;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;

import wbs.platform.media.model.ContentRec;
import wbs.platform.media.model.MediaRec;
import wbs.platform.media.model.MediaTypeRec;

public
interface MediaLogic {

	ContentRec findOrCreateContent (
			TaskLogger parentTaskLogger,
			byte[] data);

	Optional <MediaRec> createMedia (
			TaskLogger parentTaskLogger,
			byte[] data,
			String mimeType,
			String filename,
			Optional <String> encoding);

	default
	MediaRec createMediaRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data,
			@NonNull String mimeType,
			@NonNull String filename,
			@NonNull Optional <String> encoding) {

		return optionalOrThrow (
			createMedia (
				parentTaskLogger,
				data,
				mimeType,
				filename,
				encoding),
			() -> new IllegalArgumentException (
				stringFormat (
					"Unable to decode \"%s\" of type \"%s\"",
					filename,
					mimeType)));

	}

	MediaRec createMediaFromImage (
			TaskLogger parentTaskLogger,
			BufferedImage image,
			String mimeType,
			String filename);

	Optional <MediaRec> createMediaFromImage (
			TaskLogger parentTaskLogger,
			byte[] data,
			String mimeType,
			String filename);

	default
	MediaRec createMediaFromImageRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data,
			@NonNull String mimeType,
			@NonNull String filename) {

		return optionalGetRequired (
			createMediaFromImage (
				parentTaskLogger,
				data,
				mimeType,
				filename));

	}


	MediaRec createMediaWithThumbnail (
			TaskLogger parentTaskLogger,
			byte[] data,
			BufferedImage thumbnailImage,
			String mimeType,
			String filename,
			Long width,
			Long height);

	MediaRec createMediaWithThumbnail (
			TaskLogger parentTaskLogger,
			byte[] data,
			byte[] thumb100,
			byte[] thumb32,
			String mimeType,
			String filename,
			Long width,
			Long height);

	Optional <MediaRec> createMediaFromVideo (
			TaskLogger parentTaskLogger,
			byte[] data,
			String mimeType,
			String filename);

	default
	MediaRec createMediaFromVideoRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data,
			@NonNull String mimeType,
			@NonNull String filename) {

		return optionalGetRequired (
			createMediaFromVideo (
				parentTaskLogger,
				data,
				mimeType,
				filename));

	}

	MediaRec createMediaFromAudio (
			TaskLogger parentTaskLogger,
			byte[] data,
			String mimeType,
			String filename);

	MediaTypeRec findMediaTypeRequired (
			String mimeType);

	Optional <MediaRec> createTextualMedia (
			TaskLogger parentTaskLogger,
			byte[] data,
			String mimeType,
			String filename,
			String encoding);

	MediaRec createTextMedia (
			TaskLogger parentTaskLogger,
			String text,
			String mimeType,
			String filename);

	Optional <BufferedImage> readImage (
			TaskLogger parentTaskLogger,
			byte[] data,
			String mimeType);

	default
	BufferedImage readImageRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data,
			@NonNull String mimeType) {

		return optionalGetRequired (
			readImage (
				parentTaskLogger,
				data,
				mimeType));

	}


	byte[] writeImage (
			BufferedImage image,
			String mimeType);

	byte[] writeJpeg (
			BufferedImage image,
			float jpegQuality);

	default
	Optional <BufferedImage> getImage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MediaRec media) {

		return readImage (
			parentTaskLogger,
			media.getContent ().getData (),
			media.getMediaType ().getMimeType ());

	}


	default
	BufferedImage getImageRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MediaRec media) {

		return optionalGetRequired (
			getImage (
				parentTaskLogger,
				media));

	}

	BufferedImage resampleImageToFit (
			BufferedImage image,
			Long maxWidth,
			Long maxHeight);

	BufferedImage cropAndResampleImage (
			BufferedImage image,
			Long maxWidth,
			Long maxHeight);

	BufferedImage rotateImage90 (
			BufferedImage image);

	BufferedImage rotateImage180 (
			BufferedImage image);

	BufferedImage rotateImage270 (
			BufferedImage image);

	Optional <byte[]> videoConvert (
			TaskLogger parentTaskLogger,
			String profileName,
			byte[] data);

	default
	byte[] videoConvertRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String profileName,
			@NonNull byte[] data) {

		return optionalGetRequired (
			videoConvert (
				parentTaskLogger,
				profileName,
				data));

	}

	Optional <byte[]> videoFrameBytes (
			TaskLogger parentTaskLogger,
			byte[] data);

	Optional <BufferedImage> videoFrame (
			TaskLogger parentTaskLogger,
			byte[] data);

	boolean isApplication (
			String mimeType);

	boolean isApplication (
			MediaTypeRec mediaType);

	boolean isApplication (
			MediaRec media);

	boolean isAudio (
			String mimeType);

	boolean isAudio (
			MediaTypeRec mediaType);

	boolean isAudio (
			MediaRec media);

	boolean isImage (
			String mimeType);

	boolean isImage (
			MediaTypeRec mediaType);

	boolean isImage (
			MediaRec media);

	boolean isText (
			String mimeType);

	boolean isText (
			MediaTypeRec mediaType);

	boolean isText (
			MediaRec media);

	boolean isTextual (
			String mimeType);

	boolean isTextual (
			MediaTypeRec mediaType);

	boolean isTextual (
			MediaRec media);

	boolean isVideo (
			String mimeType);

	boolean isVideo (
			MediaTypeRec mediaType);

	boolean isVideo (
			MediaRec media);

	Set<String> videoProfileNames ();

}
