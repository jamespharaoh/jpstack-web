package wbs.platform.media.logic;

import java.awt.image.BufferedImage;
import java.util.Set;

import com.google.common.base.Optional;

import wbs.platform.media.model.ContentRec;
import wbs.platform.media.model.MediaRec;
import wbs.platform.media.model.MediaTypeRec;

public
interface MediaLogic {

	ContentRec findOrCreateContent (
			byte[] data);

	Optional<MediaRec> createMedia (
			byte[] data,
			String mimeType,
			String filename,
			Optional <String> encoding);

	MediaRec createMediaRequired (
			byte[] data,
			String mimeType,
			String filename,
			Optional <String> encoding);

	MediaRec createMediaFromImage (
			BufferedImage image,
			String mimeType,
			String filename);

	Optional<MediaRec> createMediaFromImage (
			byte[] data,
			String mimeType,
			String filename);

	MediaRec createMediaFromImageRequired (
			byte[] data,
			String mimeType,
			String filename);

	MediaRec createMediaWithThumbnail (
			byte[] data,
			BufferedImage thumbnailImage,
			String mimeType,
			String filename,
			Long width,
			Long height);

	MediaRec createMediaWithThumbnail (
			byte[] data,
			byte[] thumb100,
			byte[] thumb32,
			String mimeType,
			String filename,
			Long width,
			Long height);

	Optional<MediaRec> createMediaFromVideo (
			byte[] data,
			String mimeType,
			String filename);

	MediaRec createMediaFromVideoRequired (
			byte[] data,
			String mimeType,
			String filename);

	MediaRec createMediaFromAudio (
			byte[] data,
			String mimeType,
			String filename);

	MediaTypeRec findMediaTypeRequired (
			String mimeType);

	Optional<MediaRec> createTextualMedia (
			byte[] data,
			String mimeType,
			String filename,
			String encoding);

	MediaRec createTextMedia (
			String text,
			String mimeType,
			String filename);

	Optional<BufferedImage> readImage (
			byte[] data,
			String mimeType);

	BufferedImage readImageRequired (
			byte[] data,
			String mimeType);

	byte[] writeImage (
			BufferedImage image,
			String mimeType);

	byte[] writeJpeg (
			BufferedImage image,
			float jpegQuality);

	BufferedImage getImage (
			MediaRec media);

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

	Optional<byte[]> videoConvert (
			String profileName,
			byte[] data);

	byte[] videoConvertRequired (
			String profileName,
			byte[] data);

	Optional<byte[]> videoFrameBytes (
			byte[] data);

	Optional<BufferedImage> videoFrame (
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

	boolean isVideo (
			String mimeType);

	boolean isVideo (
			MediaTypeRec mediaType);

	boolean isVideo (
			MediaRec media);

	Set<String> videoProfileNames ();

}
