package wbs.platform.media.logic;

import static wbs.utils.etc.LogicUtils.allOf;
import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.Misc.iterable;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.Misc.runFilter;
import static wbs.utils.etc.Misc.runFilterAdvanced;
import static wbs.utils.etc.NumberUtils.fromJavaInteger;
import static wbs.utils.etc.NumberUtils.integerEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThan;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringToBytes;

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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.model.ContentObjectHelper;
import wbs.platform.media.model.ContentRec;
import wbs.platform.media.model.MediaObjectHelper;
import wbs.platform.media.model.MediaRec;
import wbs.platform.media.model.MediaTypeObjectHelper;
import wbs.platform.media.model.MediaTypeRec;

import wbs.utils.io.RuntimeIoException;

@SingletonComponent ("mediaLogic")
public
class MediaLogicImplementation
	implements MediaLogic {

	// singleton dependencies

	@SingletonDependency
	ContentObjectHelper contentHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaObjectHelper mediaHelper;

	@SingletonDependency
	MediaTypeObjectHelper mediaTypeHelper;

	// ffmpeg profiles

	public static abstract
	class FfmpegProfile {

		String fileExtension;

		public
		FfmpegProfile (
				@NonNull String fileExtension) {

			this.fileExtension =
				fileExtension;

		}

		public abstract
		List<List<String>> toFfmpeg ();

	}

	public static
	class FfmpegVideoProfile
		extends FfmpegProfile {

		boolean twoPass;

		Optional <String> videoCodec;
		Optional <String> videoResolution;
		Optional <String> videoBitrate;
		Optional <String> videoDuration;

		public
		FfmpegVideoProfile (
				boolean twoPass,
				@NonNull String fileExtension,
				@NonNull Optional <String> videoCodec,
				@NonNull Optional <String> videoResolution,
				@NonNull Optional <String> videoBitrate,
				@NonNull Optional <String> videoDuration) {

			super (
				fileExtension);

			this.twoPass =
				twoPass;

			this.videoCodec =
				videoCodec;

			this.videoResolution =
				videoResolution;

			this.videoBitrate =
				videoBitrate;

			this.videoDuration =
				videoDuration;

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

			if (
				optionalIsPresent (
					videoCodec)
			) {

				passOne.addAll (
					ImmutableList.<String>of (
						"-vcodec",
						videoCodec.get ()));

				passTwo.addAll (
					ImmutableList.<String>of (
						"-vcodec",
						videoCodec.get ()));
			}

			if (
				optionalIsPresent (
					videoResolution)
			) {

				passOne.addAll (
					ImmutableList.<String>of (
						"-s",
						videoResolution.get ()));

				passTwo.addAll (
					ImmutableList.<String>of (
						"-s",
						videoResolution.get ()));

			}

			if (
				optionalIsPresent (
					videoBitrate)
			) {

				passOne.addAll (
					ImmutableList.<String>of (
						"-b",
						videoBitrate.get ()));

				passTwo.addAll (
					ImmutableList.<String>of (
						"-b",
						videoBitrate.get ()));

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

			if (
				optionalIsPresent (
					videoDuration)
			) {

				passTwo.addAll (
					ImmutableList.<String>of (
						"-t",
						videoDuration.get ()));

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

		.put (
			"3gpp",
			new FfmpegVideoProfile (
				true,
				"3gp",
				Optional.of ("h263"),
				Optional.of ("128x96"),
				Optional.of ("100k"),
				Optional.of ("00:00:15")))

		.put (
			"flv",
			new FfmpegVideoProfile (
				false,
				"flv",
				Optional.of ("flv"),
				Optional.<String>absent (),
				Optional.<String>absent (),
				Optional.<String>absent ()))

		.put (
			"mp4",
			new FfmpegVideoProfile (
				false,
				"mp4",
				Optional.of ("h263"),
				Optional.<String>absent (),
				Optional.<String>absent (),
				Optional.<String>absent ()))

		.put (
			"mp3",
			new FfmpegAudioProfile (
				"mp3"))

		.build ();

	@Override
	public
	ContentRec findOrCreateContent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"findOrCreateContent");

		) {

			// work out hash code

			Long shortHash =
				fromJavaInteger (
					Arrays.hashCode (
						data));

			// look for existing content

			List <ContentRec> list =
				contentHelper.findByShortHash (
					shortHash);

			long index = 0;

			for (
				ContentRec content
					: list) {

				if (
					Arrays.equals (
						content.getData (),
						data)
				) {
					return content;
				}

				if (content.getI () >= index) {

					index =
						content.getI () + 1;

				}

			}

			// create a new content object

			ContentRec content =
				contentHelper.insert (
					taskLogger,
					contentHelper.createInstance ()

				.setData (
					data)

				.setHash (
					shortHash)

				.setI (
					index)

			);

			// return

			return content;

		}

	}

	@Override
	public
	Optional <MediaRec> createMedia (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data,
			@NonNull String mimeType,
			@NonNull String filename,
			@NonNull Optional <String> encoding) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createMedia");

		) {

			if (
				contains (
					imageTypes,
					mimeType)
			) {

				return createMediaFromImage (
					taskLogger,
					data,
					mimeType,
					filename);

			} else if (
				contains (
					textualTypes,
					mimeType)
			) {

				if (
					optionalIsNotPresent (
						encoding)
				) {

					throw new IllegalArgumentException (
						"Encoding must be specified for textual content");

				}

				return createTextualMedia (
					taskLogger,
					data,
					mimeType,
					filename,
					encoding.get ());

			} else if (
				contains (
					videoTypes,
					mimeType)
			) {

				return createMediaFromVideo (
					taskLogger,
					data,
					mimeType,
					filename);

			} else {

				throw new RuntimeException (
					stringFormat (
						"Unknown media type \"%s\"",
						mimeType));

			}

		}

	}

	private final static
	String defaultMimeType = "image/jpeg";

	@Override
	public
	MediaRec createMediaFromImage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull BufferedImage image,
			@NonNull String mimeType,
			@NonNull String filename) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createMediaFromImage");

		) {

			// encode the image

			byte[] data =
				writeImage (
					image,
					defaultMimeType);

			return createMediaWithThumbnail (
				taskLogger,
				data,
				image,
				mimeType,
				filename,
				fromJavaInteger (
					image.getWidth ()),
				fromJavaInteger (
					image.getHeight ()));

		}

	}

	@Override
	public
	Optional <MediaRec> createMediaFromImage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data,
			@NonNull String mimeType,
			@NonNull String filename) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createMediaFromImage");

		) {

			Optional <BufferedImage> imageOptional =
				readImage (
					taskLogger,
					data,
					mimeType);

			if (
				optionalIsNotPresent (
					imageOptional)
			) {
				return optionalAbsent ();
			}

			BufferedImage image =
				imageOptional.get ();

			return optionalOf (
				createMediaWithThumbnail (
					taskLogger,
					data,
					image,
					mimeType,
					filename,
					fromJavaInteger (
						image.getWidth ()),
					fromJavaInteger (
						image.getHeight ())));

		}

	}

	@Override
	public
	MediaRec createMediaWithThumbnail (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data,
			@NonNull BufferedImage thumbnailImage,
			@NonNull String mimeType,
			@NonNull String filename,
			@NonNull Long width,
			@NonNull Long height) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createMediaWithThumbnail");

		) {

			// create the 100x100 thumbnail

			BufferedImage image100 =
				resampleImageToFit (
					thumbnailImage,
					100l,
					100l);

			byte[] data100 =
				writeImage (
					image100,
					defaultMimeType);

			// create the 32x32 thumbnail

			BufferedImage image32 =
				resampleImageToFit (
					thumbnailImage,
					32l,
					32l);

			byte[] data32 =
				writeImage (
					image32,
					defaultMimeType);

			return createMediaWithThumbnail (
				taskLogger,
				data,
				data100,
				data32,
				mimeType,
				filename,
				width,
				height);

		}

	}

	@Override
	public
	MediaRec createMediaWithThumbnail (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data,
			@NonNull byte[] thumb100,
			@NonNull byte[] thumb32,
			@NonNull String mimeType,
			@NonNull String filename,
			@NonNull Long width,
			@NonNull Long height) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createMediaWithThumbnail");

		) {

			MediaTypeRec mediaType =
				findMediaTypeRequired (
					mimeType);

			return mediaHelper.insert (
				taskLogger,
				mediaHelper.createInstance ()

				.setFilename (
					filename)

				.setContent (
					findOrCreateContent (
						taskLogger,
						data))

				.setThumb100Content (
					findOrCreateContent (
						taskLogger,
						thumb100))

				.setThumb32Content (
					findOrCreateContent (
						taskLogger,
						thumb32))

				.setMediaType (
					mediaType)

				.setThumbMediaType (
					mediaType)

				.setWidth (
					width)

				.setHeight (
					height)

			);

		}

	}

	@Override
	public
	Optional <MediaRec> createMediaFromVideo (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data,
			@NonNull String mimeType,
			@NonNull String filename) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createMediaFromVideo");

		) {

			Optional <BufferedImage> videoFrameImageOptional =
				videoFrame (
					taskLogger,
					data);

			if (
				optionalIsNotPresent (
					videoFrameImageOptional)
			) {
				return optionalAbsent ();
			}

			BufferedImage videoFrameImage =
				videoFrameImageOptional.get ();

			return optionalOf (
				createMediaWithThumbnail (
					taskLogger,
					data,
					videoFrameImage,
					mimeType,
					filename,
					fromJavaInteger (
						videoFrameImage.getWidth ()),
					fromJavaInteger (
						videoFrameImage.getHeight ())));

		}

	}

	@Override
	public
	MediaRec createMediaFromAudio (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data,
			@NonNull String mimeType,
			@NonNull String filename) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createMediaFromAudio");

		) {

			MediaTypeRec mediaType =
				findMediaTypeRequired (
					mimeType);

			return mediaHelper.insert (
				taskLogger,
				mediaHelper.createInstance ()

				.setFilename (
					filename)

				.setContent (
					findOrCreateContent (
						taskLogger,
						data))

				.setMediaType (
					mediaType)

			);

		}

	}

	@Override
	public
	MediaTypeRec findMediaTypeRequired (
			@NonNull String mimeType) {

		return mediaTypeHelper.findByCodeRequired (
			GlobalId.root,
			mimeType);

	}

	@Override
	public
	Optional <MediaRec> createTextualMedia (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data,
			@NonNull String mimeType,
			@NonNull String filename,
			@NonNull String encoding) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createTextualMedia");

		) {

			return optionalOf (
				mediaHelper.insert (
					taskLogger,
					mediaHelper.createInstance ()

				.setMediaType (
					findMediaTypeRequired (
						mimeType))

				.setContent (
					findOrCreateContent (
						taskLogger,
						data))

				.setFilename (
					filename)

				.setEncoding (
					encoding)

			));

		}

	}

	@Override
	public
	MediaRec createTextMedia (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String text,
			@NonNull String mimeType,
			@NonNull String filename) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createTextMedia");

		) {

			return mediaHelper.insert (
				taskLogger,
				mediaHelper.createInstance ()

				.setMediaType (
					findMediaTypeRequired (
						mimeType))

				.setContent (
					findOrCreateContent (
						taskLogger,
						stringToBytes (
							text,
							"utf-8")))

				.setFilename (
					filename)

				.setEncoding (
					"utf-8")

			);

		}

	}

	// TODO surely this exists in a standard library
	private static
	class ByteArrayImageInputStream
		extends ImageInputStreamImpl {

		byte[] data;

		ByteArrayImageInputStream (
				@NonNull byte[] newData) {

			data = newData;

		}

		@Override
		public
		int read () {

			if (streamPos < data.length) {

				return Byte.toUnsignedInt (
					data [
						toJavaIntegerRequired (
							streamPos ++)]);

			} else {

				return -1;

			}

		}

		@Override
		public
		int read (
				@NonNull byte[] bytes,
				int offset,
				int length) {

			if (streamPos + length <= data.length) {

				System.arraycopy (
					data,
					toJavaIntegerRequired (
						streamPos),
					bytes,
					offset,
					length);

				streamPos +=
					length;

				return length;

			} else if (streamPos < data.length) {

				int numread =
					toJavaIntegerRequired (
						+ data.length
						- streamPos);

				System.arraycopy (
					data,
					toJavaIntegerRequired (
						streamPos),
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

	@Override
	public
	Optional <BufferedImage> readImage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data,
			@NonNull String mimeType) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"readImage");

		) {

			for (
				ImageReader imageReader
					: iterable (
						ImageIO.getImageReadersByMIMEType (
							mimeType))
			) {

				taskLogger.debugFormat (
					"Attempt to read image of type %s with %s bytes with %s",
					mimeType,
					integerToDecimalString (
						data.length),
					imageReader.toString ());

				imageReader.setInput (
					new ByteArrayImageInputStream (data));

				try {

					return Optional.of (
						imageReader.read (0));

				} catch (IOException exception) {

					taskLogger.warningFormatException (
						exception,
						"Failed to read image of type %s with %s bytes",
						mimeType,
						integerToDecimalString (
							data.length));

				}

			}

			taskLogger.warningFormat (
				"Exhausted options to read image of type %s with %s bytes",
				mimeType,
				integerToDecimalString (
					data.length));

			return optionalAbsent ();

		}

	}

	@Override
	public
	byte[] writeImage (
			@NonNull BufferedImage image,
			@NonNull String mimeType) {

		for (
			ImageWriter imageWriter
				: iterable (
					ImageIO.getImageWritersByMIMEType (
						mimeType))
		) {

			try (

				ByteArrayOutputStream byteArrayOutputStream =
					new ByteArrayOutputStream ();

				MemoryCacheImageOutputStream imageOutputStream =
					new MemoryCacheImageOutputStream (
						byteArrayOutputStream);

			) {

				imageWriter.setOutput (
					imageOutputStream);

				try {

					imageWriter.write (
						new IIOImage (
							image,
							null,
							null));

					return byteArrayOutputStream.toByteArray ();

				} catch (IOException exception) {

				}

			} catch (IOException ioException) {

				throw new RuntimeException (
					ioException);

			}

		}

		throw new RuntimeException ();

	}

	@Override
	public
	byte[] writeJpeg (
			@NonNull BufferedImage image,
			float jpegQuality) {

		for (
			ImageWriter imageWriter
				: iterable (
					ImageIO.getImageWritersByMIMEType (
						"image/jpeg"))
		) {

			try (

				ByteArrayOutputStream byteArrayOutputStream =
					new ByteArrayOutputStream ();

				MemoryCacheImageOutputStream imageOutputStream =
					new MemoryCacheImageOutputStream (
							byteArrayOutputStream);

			) {

				imageWriter.setOutput (
					imageOutputStream);

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

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

		}

		throw new RuntimeException ();

	}

	@Override
	public
	BufferedImage resampleImageToFit (
			@NonNull BufferedImage image,
			@NonNull Long maxWidth,
			@NonNull Long maxHeight) {

		// same image if already fits

		if (allOf (

			() -> lessThan (
				image.getWidth (),
				maxWidth),

			() -> lessThan (
				image.getHeight (),
				maxHeight)

		)) {

			return image;

		}

		// start with max target size

		long targetWidth =
			image.getWidth ();

		long targetHeight =
			image.getHeight ();

		// reduce height to maintain ratio

		if (targetWidth > maxWidth) {

			targetWidth =
				maxWidth;

			targetHeight =
				image.getHeight () * maxWidth / image.getWidth ();

		}

		// reduce width to maintain ratio

		if (targetHeight > maxHeight) {

			targetHeight =
				maxHeight;

			targetWidth =
				image.getWidth () * maxHeight / image.getHeight ();

		}

		// determine image type

		int imageType =
			image.getType ();

		if (imageType == BufferedImage.TYPE_CUSTOM) {

			imageType =
				BufferedImage.TYPE_INT_RGB;

		}

		// resample image

		BufferedImage targetImage =
			new BufferedImage (
				toJavaIntegerRequired (
					targetWidth),
				toJavaIntegerRequired (
					targetHeight),
				imageType);

		Graphics2D graphics =
			targetImage.createGraphics ();

		graphics.drawImage (
			image,
			0,
			0,
			toJavaIntegerRequired (
				targetWidth),
			toJavaIntegerRequired (
				targetHeight),
			0,
			0,
			image.getWidth (),
			image.getHeight (),
			null);

		graphics.dispose ();

		return targetImage;

	}

	@Override
	public
	BufferedImage cropAndResampleImage (
			@NonNull BufferedImage sourceImage,
			@NonNull Long targetWidth,
			@NonNull Long targetHeight) {

		// same image if already fits

		if (allOf (

			() -> integerEqualSafe (
				sourceImage.getWidth (),
				targetWidth),

			() -> integerEqualSafe (
				sourceImage.getHeight (),
				targetHeight)

		)) {

			return sourceImage;

		}

		// select crop type

		long sourceRatio =
			sourceImage.getWidth ()
				* targetHeight;

		long targetRatio =
			targetWidth
				* sourceImage.getHeight ();

		long sourceWidth;
		long sourceHeight;

		if (
			integerEqualSafe (
				sourceRatio,
				targetRatio)
		) {

			// keep same ratio

			sourceWidth =
				sourceImage.getWidth ();

			sourceHeight =
				sourceImage.getHeight ();

		} else if (
			moreThan (
				sourceRatio,
				targetRatio)
		) {

			// reduce width

			sourceWidth =
				sourceImage.getHeight ()
					* targetWidth
					/ targetHeight;

			sourceHeight =
				sourceImage.getHeight ();

		} else if (
			lessThan (
				sourceRatio,
				targetRatio)
		) {

			// reduce height

			sourceWidth =
				sourceImage.getWidth ();

			sourceHeight =
				sourceImage.getWidth ()
					* targetHeight
					/ targetWidth;

		} else {

			throw new RuntimeException (
				"LOGIC ERROR");

		}

		long sourceOffsetHorizontal =
			(sourceImage.getWidth () - sourceWidth) / 2l;

		long sourceOffsetVertical =
			(sourceImage.getHeight () - sourceHeight) / 2l;

		// determine image type

		int imageType =
			sourceImage.getType ();

		if (imageType == BufferedImage.TYPE_CUSTOM) {

			imageType =
				BufferedImage.TYPE_INT_RGB;

		}

		// resample image

		BufferedImage targetImage =
			new BufferedImage (
				toJavaIntegerRequired (
					targetWidth),
				toJavaIntegerRequired (
					targetHeight),
				imageType);

		Graphics2D graphics =
			targetImage.createGraphics ();

		graphics.drawImage (
			sourceImage,
			0,
			0,
			toJavaIntegerRequired (
				targetWidth),
			toJavaIntegerRequired (
				targetHeight),
			toJavaIntegerRequired (
				sourceOffsetHorizontal),
			toJavaIntegerRequired (
				sourceOffsetVertical),
			toJavaIntegerRequired (
				sourceOffsetHorizontal + sourceWidth),
			toJavaIntegerRequired (
				sourceOffsetVertical + sourceHeight),
			null);

		graphics.dispose ();

		return targetImage;

	}

	@Override
	public
	BufferedImage rotateImage90 (
			@NonNull BufferedImage image) {

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

	@Override
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

	@Override
	public
	BufferedImage rotateImage270 (
			@NonNull BufferedImage image) {

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

	@Override
	public
	Optional <byte[]> videoConvert (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String profileName,
			@NonNull byte[] data) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"videoConvert");

		) {

			FfmpegProfile ffmpegProfile =
				ffmpegProfiles.get (
					profileName);

			try {

				return optionalOf (
					runFilterAdvanced (
						taskLogger,
						data,
						"",
						"." + ffmpegProfile.fileExtension,
						ffmpegProfile.toFfmpeg ()));

			} catch (Exception exception) {

				return optionalAbsent ();

			}

		}

	}

	@Override
	public
	Optional <byte[]> videoFrameBytes (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"videoFrameBytes");

		) {

			return optionalOf (
				runFilter (
					taskLogger,
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
					"<out>"));

		} catch (InterruptedException interruptedException) {

			Thread.currentThread ().interrupt ();

			return optionalAbsent ();

		}

	}

	@Override
	public
	Optional <BufferedImage> videoFrame (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"videoFrame");

		) {

			Optional <byte[]> videoFrameBytesOptional =
				videoFrameBytes (
					taskLogger,
					data);

			if (
				optionalIsNotPresent (
					videoFrameBytesOptional)
			) {
				return optionalAbsent ();
			}

			return readImage (
				taskLogger,
				videoFrameBytesOptional.get (),
				"image/jpeg");

		}

	}

	@Override
	public
	boolean isApplication (
			@NonNull String mimeType) {

		return applicationTypes.contains (
			mimeType);

	}

	@Override
	public
	boolean isApplication (
			@NonNull MediaTypeRec mediaType) {

		return isApplication (
			mediaType.getMimeType ());

	}

	@Override
	public
	boolean isApplication (
			@NonNull MediaRec media) {

		return isApplication (
			media.getMediaType ());

	}

	@Override
	public
	boolean isAudio (
			@NonNull String mimeType) {

		return audioTypes.contains (
			mimeType);

	}

	@Override
	public
	boolean isAudio (
			@NonNull MediaTypeRec mediaType) {

		return isAudio (
			mediaType.getMimeType ());

	}

	@Override
	public
	boolean isAudio (
			@NonNull MediaRec media) {

		return isAudio (
			media.getMediaType ());

	}

	@Override
	public
	boolean isImage (
			@NonNull String mimeType) {

		return imageTypes.contains (
			mimeType);

	}

	@Override
	public
	boolean isImage (
			@NonNull MediaTypeRec mediaType) {

		return isImage (
			mediaType.getMimeType ());

	}

	@Override
	public
	boolean isImage (
			@NonNull MediaRec media) {

		return isImage (
			media.getMediaType ());

	}

	@Override
	public
	boolean isText (
			@NonNull String mimeType) {

		return textTypes.contains (
			mimeType);

	}

	@Override
	public
	boolean isText (
			@NonNull MediaTypeRec mediaType) {

		return isText (
			mediaType.getMimeType ());

	}

	@Override
	public
	boolean isText (
			@NonNull MediaRec media) {

		return isText (
			media.getMediaType ());

	}

	@Override
	public
	boolean isTextual (
			@NonNull String mimeType) {

		return textualTypes.contains (
			mimeType);

	}

	@Override
	public
	boolean isTextual (
			@NonNull MediaTypeRec mediaType) {

		return isTextual (
			mediaType.getMimeType ());

	}

	@Override
	public
	boolean isTextual (
			@NonNull MediaRec media) {

		return isTextual (
			media.getMediaType ());

	}

	@Override
	public
	boolean isVideo (
			@NonNull String mimeType) {

		return videoTypes.contains (
			mimeType);

	}

	@Override
	public
	boolean isVideo (
			@NonNull MediaTypeRec mediaType) {

		return isVideo (
			mediaType.getMimeType ());

	}

	@Override
	public
	boolean isVideo (
			@NonNull MediaRec media) {

		return isVideo (
			media.getMediaType ());

	}

	@Override
	public
	Set<String> videoProfileNames () {

		return ffmpegProfiles.keySet ();

	}

	// data

	public final static
	Set <String> imageTypes =
		ImmutableSet.of (
			"image/gif",
			"image/jpeg",
			"image/jpg",
			"image/png");

	public final static
	Set <String> textTypes =
		ImmutableSet.of (
			"text/plain");

	public final static
	Set <String> applicationTypes =
		ImmutableSet.of (
			"application/smil");

	public final static
	Set <String> videoTypes =
		ImmutableSet.of (
			"video/3gpp",
			"video/mpeg",
			"video/mp4");

	public final static
	Set <String> audioTypes =
		ImmutableSet.of (
			"audio/mpeg");

	public final static
	Set <String> textualTypes =
		ImmutableSet.of (
			"text/plain",
			"application/smil");

}
