package wbs.platform.media.logic;

import static wbs.framework.utils.etc.Misc.allOf;
import static wbs.framework.utils.etc.Misc.iterable;
import static wbs.framework.utils.etc.Misc.runFilter;
import static wbs.framework.utils.etc.Misc.runFilterAdvanced;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.stringToBytes;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStreamImpl;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.inject.Inject;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.GlobalId;
import wbs.platform.media.model.ContentObjectHelper;
import wbs.platform.media.model.ContentRec;
import wbs.platform.media.model.MediaObjectHelper;
import wbs.platform.media.model.MediaRec;
import wbs.platform.media.model.MediaTypeObjectHelper;
import wbs.platform.media.model.MediaTypeRec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

@Log4j
@SingletonComponent ("mediaLogic")
public
class MediaLogicImpl
	implements MediaLogic {

	// dependencies

	@Inject
	ContentObjectHelper contentHelper;

	@Inject
	MediaObjectHelper mediaHelper;

	@Inject
	MediaTypeObjectHelper mediaTypeHelper;

	// ffmpeg profiles

	public static abstract
	class FfmpegProfile {
		final String fileExtension;

		public
		FfmpegProfile (
				String fileExtension) {

			this.fileExtension = fileExtension;

		}

		public abstract
		List<List<String>> toFfmpeg ();

	}

	public static
	class FfmpegVideoProfile
		extends FfmpegProfile {

		boolean twoPass;
		String videoCodec;
		String videoResolution;
		String videoBitrate;
		String videoDuration;

		public
		FfmpegVideoProfile (
				boolean twoPass,
				String fileExtension,
				String videoCodec,
				String videoResolution,
				String videoBitrate,
				String videoDuration) {

			super (fileExtension);

			this.twoPass = twoPass;
			this.videoCodec = videoCodec;
			this.videoResolution = videoResolution;
			this.videoBitrate = videoBitrate;
			this.videoDuration = videoDuration;

		}

		@Override
		public
		List<List<String>> toFfmpeg () {

			List<String> passOne =
				new ArrayList<String> ();

			List<String> passTwo =
				new ArrayList<String> ();

			// input

			passOne.addAll (
				ImmutableList.<String>of (
					"ffmpeg",
					"-y",
					"-i",
					"<in>"));

			passTwo.addAll (
				ImmutableList.<String>of (
					"ffmpeg",
					"-y",
					"-i",
					"<in>"));

			// video

			if (videoCodec != null) {

				passOne.addAll (
					ImmutableList.<String>of (
						"-vcodec",
						videoCodec));

				passTwo.addAll (
					ImmutableList.<String>of (
						"-vcodec",
						videoCodec));
			}

			if (videoResolution != null) {

				passOne.addAll (
					ImmutableList.<String>of (
						"-s",
						videoResolution));

				passTwo.addAll (
					ImmutableList.<String>of (
						"-s",
						videoResolution));
			}

			if (videoBitrate != null) {

				passOne.addAll (
					ImmutableList.<String>of (
						"-b",
						videoBitrate));

				passTwo.addAll (
					ImmutableList.<String>of (
						"-b",
						videoBitrate));
			}

			// audio

			passOne.addAll (
				ImmutableList.<String>of (
					"-an"));

			passTwo.addAll (
				ImmutableList.<String>of (
					"-an"));

			// two pass

			if (twoPass) {

				passOne.addAll (
					ImmutableList.<String>of (
						"-pass",
						"1"));

				passTwo.addAll (
					ImmutableList.<String>of (
						"-pass",
						"2"));
			}

			// duration

			if (videoDuration != null) {

				passTwo.addAll (
					ImmutableList.<String>of (
						"-t",
						videoDuration));
			}

			// output

			passOne.addAll (
				ImmutableList.<String>of (
					"<out>"));

			passTwo.addAll (
				ImmutableList.<String>of ("<out>"));

			// return

			return twoPass

				? ImmutableList.<List<String>>of (
					passOne,
					passTwo)

				: ImmutableList.<List<String>>of (
					passTwo);
		}
	}

	public static
	class FfmpegAudioProfile
		extends FfmpegProfile {

		public
		FfmpegAudioProfile (
				String fileExtension) {

			super (fileExtension);

		}

		@Override
		public
		List<List<String>> toFfmpeg () {

			return ImmutableList.<List<String>>of (

				ImmutableList.<String>of (
					"ffmpeg",
					"-y",
					"-i",
					"<in>"),

				ImmutableList.<String>of (
					"<out>"));

		}

	}

	public final static
	Map<String,FfmpegProfile> ffmpegProfiles =
		ImmutableMap.<String,FfmpegProfile>builder ()

			.put ("3gpp",
				new FfmpegVideoProfile (
					true,
					"3gp",
					"h263",
					"128x96",
					"100k",
					"00:00:15"))

			.put ("flv",
				new FfmpegVideoProfile (
					false,
					"flv",
					"flv",
					null,
					null,
					null))

			.put ("mp4",
				new FfmpegVideoProfile (
					false,
					"mp4",
					"h263",
					null,
					null,
					null))

			.put ("mp3",
				new FfmpegAudioProfile (
					"mp3"))

			.build ();

	public
	ContentRec findOrCreateContent (
			byte[] data) {

		// work out hash code

		int hash =
			Arrays.hashCode (data);

		// look for existing content

		List<ContentRec> list =
			contentHelper.findByHash (
				hash);

		int i = 0;
		for (ContentRec content : list) {
			if (Arrays.equals(content.getData(), data))
				return content;
			if (content.getI() >= i)
				i = content.getI() + 1;
		}

		// create a new content object

		ContentRec content =
			contentHelper.insert (
				new ContentRec ()
					.setData (data)
					.setHash (hash)
					.setI (i));

		// return

		return content;

	}

	public
	MediaRec createMedia (
			byte[] data,
			String mimeType,
			String filename,
			String encoding) {

		if (imageTypes.contains (mimeType)) {

			return createMediaFromImage (
				data,
				mimeType,
				filename);

		}

		if (textTypes.contains (mimeType)) {

			return createTextMedia (
				data,
				mimeType,
				filename,
				encoding);

		}

		if (videoTypes.contains (mimeType)) {

			return createMediaFromVideo (
				data,
				mimeType,
				filename);

		}

		throw new RuntimeException (
			stringFormat (
				"Unknown media type \"%s\"",
				mimeType));

	}

	private final static
	String defaultMimeType = "image/jpeg";

	public
	MediaRec createMediaFromImage (
			BufferedImage image,
			String mimeType,
			String filename) {

		// encode the image

		byte[] data =
			writeImage (
				image,
				defaultMimeType);

		if (data == null) {

			throw new RuntimeException (
				stringFormat (
					"Couldn't write image back as %s",
					defaultMimeType));

		}

		return createMediaWithThumbnail (
			data,
			image,
			mimeType,
			filename);

	}

	public
	MediaRec createMediaFromImage (
			byte[] data,
			String mimeType,
			String filename) {

		BufferedImage image =
			readImage (
				data,
				mimeType);

		return createMediaWithThumbnail (
			data,
			image,
			mimeType,
			filename);

	}

	public
	MediaRec createMediaWithThumbnail (
			byte[] data,
			BufferedImage thumbnailImage,
			String mimeType,
			String filename) {

		// create the 100x100 thumbnail

		BufferedImage image100 =
			resampleImage (
				thumbnailImage,
				100,
				100);

		byte[] data100 =
			writeImage (
				image100,
				defaultMimeType);

		if (data == null) {

			throw new RuntimeException (
				stringFormat (
					"Couldn't write thumbnail back as %s",
					defaultMimeType));

		}

		// create the 32x32 thumbnail

		BufferedImage image32 =
			resampleImage (
				thumbnailImage,
				32,
				32);

		byte[] data32 =
			writeImage (
				image32,
				defaultMimeType);

		if (data32 == null) {

			throw new RuntimeException (
				stringFormat (
					"Couldn't write thumbnail back as %s",
					defaultMimeType));

		}

		return createMediaWithThumbnail (
			data,
			data100,
			data32,
			mimeType,
			filename);

	}

	public
	MediaRec createMediaWithThumbnail (
			byte[] data,
			byte[] thumb100,
			byte[] thumb32,
			String mimeType,
			String filename) {

		MediaTypeRec mediaType =
			findMediaTypeRequired (mimeType);

		return mediaHelper.insert (
			new MediaRec ()

			.setFilename (
				filename)

			.setContent (
				findOrCreateContent (
					data))

			.setThumb100Content (
				findOrCreateContent (
					thumb100))

			.setThumb32Content (
				findOrCreateContent (
					thumb32))

			.setMediaType (
				mediaType)

			.setThumbMediaType (
				mediaType)

		);

	}

	public MediaRec createMediaFromVideo (
			byte[] data,
			String mimeType,
			String filename) {

		return createMediaWithThumbnail (
			data,
			videoFrame (data),
			mimeType,
			filename);

	}

	public
	MediaRec createMediaFromAudio (
			byte[] data,
			String mimeType,
			String filename) {

		MediaTypeRec mediaType =
			findMediaTypeRequired (mimeType);

		return mediaHelper.insert (
			new MediaRec ()

				.setFilename (filename)
				.setContent (findOrCreateContent (data))

				.setMediaType (mediaType));

	}

	public
	MediaTypeRec findMediaTypeRequired (
			String mimeType) {

		MediaTypeRec mediaType =
			mediaTypeHelper.findByCode (
				GlobalId.root,
				mimeType);

		if (mediaType == null) {

			throw new RuntimeException (
				"Unkown mime type: " + mimeType);

		}

		return mediaType;

	}

	public
	MediaRec createTextMedia (
			byte[] data,
			String mimeType,
			String filename,
			String encoding) {

		return mediaHelper.insert (
			new MediaRec ()

			.setMediaType (
				findMediaTypeRequired (
					mimeType))

			.setContent (
				findOrCreateContent (
					data))

			.setFilename (
				filename)

			.setEncoding (
				encoding)

		);

	}

	public
	MediaRec createTextMedia (
			String text,
			String mimeType,
			String filename) {

		return mediaHelper.insert (
			new MediaRec ()

			.setMediaType (
				findMediaTypeRequired (
					mimeType))

			.setContent (
				findOrCreateContent (
					stringToBytes (
						text,
						"utf-8")))

			.setFilename (
				filename)

			.setEncoding (
				"utf-8")

		);

	}

	// TODO surely this exists in a standard library
	private static
	class ByteArrayImageInputStream
		extends ImageInputStreamImpl {

		byte[] data;

		ByteArrayImageInputStream (
				byte[] newData) {

			data = newData;

		}

		@Override
		public
		int read () {

			return streamPos < data.length
				? data [(int) (streamPos++)] & 0x000000ff
				: -1;

		}

		@Override
		public
		int read (
				byte[] bytes,
				int offset,
				int length) {

			if (streamPos + length <= data.length) {

				System.arraycopy (
					data,
					(int) streamPos,
					bytes,
					offset,
					length);

				streamPos +=
					length;

				return length;

			} else if (streamPos < data.length) {

				int numread =
					data.length - (int) streamPos;

				System.arraycopy (
					data,
					(int) streamPos,
					bytes,
					offset,
					numread);

				streamPos =
					data.length;

				return numread;

			}

			return -1;

		}

	}

	public
	BufferedImage readImage (
			byte[] data,
			String mimeType) {

		for (ImageReader imageReader
				: iterable (
					ImageIO.getImageReadersByMIMEType (
						mimeType))) {

			log.debug (
				stringFormat (
					"Attempt to read image of type %s with %s bytes with %s",
					mimeType,
					data.length,
					imageReader.toString ()));

			imageReader.setInput (
				new ByteArrayImageInputStream (data));

			try {

				return imageReader.read (0);

			} catch (IOException exception) {

				log.warn (
					stringFormat (
						"Failed to read image of type %s with %s bytes",
						mimeType,
						data.length),
					exception);

			}

		}

		log.warn (
			stringFormat (
				"Exhausted options to read image of type %s with %s bytes",
				mimeType,
				data.length));

		return null;

	}

	public
	byte[] writeImage (
			BufferedImage image,
			String mimeType) {

		for (ImageWriter imageWriter
				: iterable (
					ImageIO.getImageWritersByMIMEType (
						mimeType))) {

			ByteArrayOutputStream byteArrayOutputStream =
				new ByteArrayOutputStream ();

			imageWriter.setOutput (
				new MemoryCacheImageOutputStream (
					byteArrayOutputStream));

			try {

				imageWriter.write (
					new IIOImage (
						image,
						null,
						null));

				return byteArrayOutputStream.toByteArray ();

			} catch (IOException exception) {

			}

		}

		return null;

	}

	public
	byte[] writeJpeg (
			BufferedImage image,
			float jpegQuality) {

		for (ImageWriter imageWriter
				: iterable (
					ImageIO.getImageWritersByMIMEType (
						"image/jpeg"))) {

			ByteArrayOutputStream byteArrayOutputStream =
				new ByteArrayOutputStream ();

			imageWriter.setOutput (
				new MemoryCacheImageOutputStream (
					byteArrayOutputStream));

			ImageWriteParam imageWriteParam =
				imageWriter.getDefaultWriteParam ();

			imageWriteParam.setCompressionMode (
				ImageWriteParam.MODE_EXPLICIT);

			imageWriteParam.setCompressionQuality (
				jpegQuality);

			try {

				imageWriter.write (
					null,
					new IIOImage (
						image,
						null,
						null),
					imageWriteParam);

				return byteArrayOutputStream.toByteArray ();

			} catch (IOException exception) {

			}

		}

		return null;

	}

	public
	BufferedImage getImage (
			MediaRec media) {

		return readImage (
			media.getContent ().getData (),
			media.getMediaType ().getMimeType ());

	}

	public
	BufferedImage resampleImage (
			@NonNull BufferedImage image,
			int maxWidth,
			int maxHeight) {

		if (
			allOf (
				image.getWidth () < maxWidth,
				image.getHeight () < maxHeight)
		) {

			return image;

		}

		int newWidth =
			image.getWidth ();

		int newHeight =
			image.getHeight ();

		if (maxWidth > 0 && newWidth > maxWidth) {

			newWidth =
				maxWidth;

			newHeight =
				image.getHeight () * maxWidth / image.getWidth ();

		}

		if (maxHeight > 0 && newHeight > maxHeight) {

			newHeight =
				maxHeight;

			newWidth =
				image.getWidth () * maxHeight / image.getHeight ();

		}

		// determine image type

		int imageType =
			image.getType ();

		if (imageType == BufferedImage.TYPE_CUSTOM) {

			imageType =
				BufferedImage.TYPE_INT_RGB;

		}

		BufferedImage newImage =
			new BufferedImage (
				newWidth,
				newHeight,
				imageType);

		Graphics2D graphics =
			newImage.createGraphics ();

		graphics.drawImage (
			image,
			0,
			0,
			newWidth,
			newHeight,
			0,
			0,
			image.getWidth (),
			image.getHeight (),
			null);

		graphics.dispose ();

		return newImage;

	}

	public
	BufferedImage rotateImage90 (
			BufferedImage image) {

		BufferedImage newImage =
			new BufferedImage (
				image.getHeight (),
				image.getWidth (),
				image.getType ());

		Graphics2D graphics =
			newImage.createGraphics ();

		graphics.translate (
			image.getHeight(),
			0);

		graphics.rotate (
			Math.PI / 2);

		graphics.drawImage (
			image,
			0,
			0,
			null);

		graphics.dispose ();

		return newImage;

	}

	public
	BufferedImage rotateImage180 (
			BufferedImage image) {

		BufferedImage newImage =
			new BufferedImage (
				image.getWidth (),
				image.getHeight (),
				image.getType ());

		Graphics2D graphics =
			newImage.createGraphics ();

		graphics.translate (
			image.getWidth (),
			image.getHeight ());

		graphics.rotate (
			Math.PI);

		graphics.drawImage (
			image,
			0,
			0,
			null);

		graphics.dispose ();

		return newImage;

	}

	public
	BufferedImage rotateImage270 (
			BufferedImage image) {

		BufferedImage newImage =
			new BufferedImage (
				image.getHeight (),
				image.getWidth (),
				image.getType ());

		Graphics2D graphics =
			newImage.createGraphics ();

		graphics.translate (
			0,
			image.getWidth ());

		graphics.rotate (
			- Math.PI / 2);

		graphics.drawImage (
			image,
			0,
			0,
			null);

		graphics.dispose ();

		return newImage;

	}

	public
	byte[] videoConvert (
			String profileName,
			byte[] data) {

		FfmpegProfile ffmpegProfile =
			ffmpegProfiles.get (profileName);

		return runFilterAdvanced (
			log,
			data,
			"",
			"." + ffmpegProfile.fileExtension,
			ffmpegProfile.toFfmpeg ());

	}

	public
	byte[] videoFrameBytes (
			byte[] data) {

		return runFilter (
			log,
			data,
			".mp4",
			".mjpeg",
			"ffmpeg",
			"-y",
			"-i",
			"<in>",
			"-vcodec",
			"mjpeg",
			"-vframes",
			"1",
			"<out>");

	}

	public
	BufferedImage videoFrame (
			byte[] data) {

		return readImage (
			videoFrameBytes (data),
			"image/jpeg");

	}

	public
	boolean isApplication (
			String mimeType) {

		return applicationTypes.contains (
			mimeType);

	}

	public
	boolean isApplication (
			MediaTypeRec mediaType) {

		return isApplication (
			mediaType.getMimeType ());

	}

	public
	boolean isApplication (
			MediaRec media) {

		return isApplication (
			media.getMediaType ());

	}

	public
	boolean isAudio (
			String mimeType) {

		return audioTypes.contains (
			mimeType);

	}

	public
	boolean isAudio (
			MediaTypeRec mediaType) {

		return isAudio (
			mediaType.getMimeType ());

	}

	public
	boolean isAudio (
			MediaRec media) {

		return isAudio (
			media.getMediaType ());

	}

	public
	boolean isImage (
			String mimeType) {

		return imageTypes.contains (
			mimeType);

	}

	public
	boolean isImage (
			MediaTypeRec mediaType) {

		return isImage (
			mediaType.getMimeType ());

	}

	public
	boolean isImage (
			MediaRec media) {

		return isImage (
			media.getMediaType ());

	}

	public
	boolean isText (
			String mimeType) {

		return textTypes.contains (
			mimeType);

	}

	public
	boolean isText (
			MediaTypeRec mediaType) {

		return isText (
			mediaType.getMimeType ());

	}

	public
	boolean isText (
			MediaRec media) {

		return isText (
			media.getMediaType ());

	}

	public
	boolean isVideo (
			String mimeType) {

		return videoTypes.contains (
			mimeType);

	}

	public
	boolean isVideo (
			MediaTypeRec mediaType) {

		return isVideo (
			mediaType.getMimeType ());

	}

	public
	boolean isVideo (
			MediaRec media) {

		return isVideo (
			media.getMediaType ());

	}

	public
	Set<String> videoProfileNames () {

		return ffmpegProfiles.keySet ();

	}

	// data

	public final static
	Set<String> imageTypes =
		ImmutableSet.<String>of (
			"image/gif",
			"image/jpeg",
			"image/jpg",
			"image/png");

	public final static
	Set<String> textTypes =
		ImmutableSet.<String>of (
			"text/plain");

	public final static
	Set<String> applicationTypes =
		ImmutableSet.<String>of (
			"application/smil");

	public final static
	Set<String> videoTypes =
		ImmutableSet.<String>of (
			"video/3gpp",
			"video/mpeg",
			"video/mp4");

	public final static
	Set<String> audioTypes =
		ImmutableSet.<String>of (
			"audio/mpeg");

}
