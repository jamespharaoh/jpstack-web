package wbs.platform.media.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.StringUtils.bytesToString;
import static wbs.framework.utils.etc.StringUtils.joinWithSemicolonAndSpace;

import java.util.Map;

import javax.inject.Inject;

import lombok.NonNull;

import com.google.common.collect.ImmutableMap;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.media.model.MediaTypeRec;

@SingletonComponent ("mediaConsoleLogic")
public
class MediaConsoleLogicImplementation
	implements MediaConsoleLogic {

	// dependencies

	@Inject
	MediaConsoleHelper mediaHelper;

	@Inject
	MediaLogic mediaLogic;

	@Inject
	ConsoleRequestContext requestContext;

	public final static
	String PLAYER_COUNT_KEY =
		"MEDIA_CONSOLE_MODULE_PLAYER_COUNT";

	// implementation

	@Override
	public
	String mediaUrl (
			@NonNull MediaRec media) {

		if (
			mediaLogic.isImage (
				media)
		) {

			return stringFormat (
				"%s",
				mediaHelper.getDefaultContextPath (
					media),
				"/media.image");

		} else {

			throw new RuntimeException (
				stringFormat (
					"Don't know how to create media URL for %s",
					media.getMediaType ().getMimeType ()));

		}

	}

	@Override
	public
	String mediaContent (
			MediaRec media,
			String rotate) {

		if (media == null)
			return "(none)";

		String mimeType =
			media.getMediaType ().getMimeType ();

		if (
			mediaLogic.isText (
				mimeType)
		) {

			return

				stringFormat (
					"%h",
					bytesToString (
						media.getContent ().getData (),
						media.getEncoding ()));

		} else if (
			mediaLogic.isVideo (
				mimeType)
		) {

			Integer playerCount =
				requestContext.requestIntRequired (
					PLAYER_COUNT_KEY);

			StringBuilder stringBuilder =
				new StringBuilder ();

			if (playerCount == null) {

				stringBuilder.append (
					stringFormat (
						"<script",
						" src=\"/flowplayer-3.1.2.min.js\"",
						"></script>\n"));

				playerCount = 0;

			}

			stringBuilder.append (
				stringFormat (
					"<a",

					" href=\"%h\"",
					stringFormat (
						"%s",
						mediaHelper.getDefaultContextPath (media),
						"/media.video"),

					" style=\"%h\"",
					joinWithSemicolonAndSpace (
						"display: block",
						"width: 300px",
						"height: 225px;"),

					" id=\"player%d\"",
					playerCount,

					"></a>\n"));

			stringBuilder.append (
				stringFormat (
					"<script",
					" type=\"text/javascript\"",
					">%s</script>\n",
					stringFormat (
						"flowplayer ('%j', '%j', {});\n",
						stringFormat (
							"player%d'",
							playerCount),
						"/flowplayer-3.1.2.swf")));

			requestContext.request (
				PLAYER_COUNT_KEY,
				playerCount + 1);

			return stringBuilder.toString ();

		} else if (
			mediaLogic.isAudio (mimeType)
		) {

			Integer playerCount =
				requestContext.requestIntRequired (
					PLAYER_COUNT_KEY);

			StringBuilder stringBuilder =
				new StringBuilder ();

			if (playerCount == null) {

				stringBuilder.append (
					stringFormat (
						"<script",
						" src=\"/flowplayer-3.1.2.min.js\"",
						"></script>\n"));

				playerCount = 0;

			}

			stringBuilder.append (
				stringFormat (

					"<a",

					" href=\"%h\"",
					stringFormat (
						"%s",
						mediaHelper.getDefaultContextPath (media),
						"/media.audio.mp3"),

					" style=\"%h\"",
					joinWithSemicolonAndSpace (
						"display: block",
						"width: 300px",
						"height: 60px;"),

					" id=\"player%d\"",
						playerCount,

					"></a>\n"));

			stringBuilder.append (
				stringFormat (
					"<script type=\"text/javascript\">\n",
					"  flowplayer ('player%d', '/flowplayer-3.1.2.swf', {\n",
					playerCount,
					"    plugins: {\n",
					"      audio: {\n",
					"        url: '/flowplayer.audio-3.1.2.swf'\n",
					"      },\n",
					"      controls: {\n",
					"        autoHide: false,\n",
					"      },\n",
					"    },\n",
					"    clip: {\n",
					"      type: 'audio',\n",
					"    }\n,",
					"  });\n",
					"</script>\n"));

			requestContext.request (
				PLAYER_COUNT_KEY,
				playerCount + 1);

			return stringBuilder.toString ();

		} else if (
			mediaLogic.isImage (
				mimeType)
		) {

			return stringFormat (
				"<img",

				" src=\"%h\"",
				stringFormat (
					"%s",
					mediaHelper.getDefaultContextPath (media),
					"/media.image",
					rotate != null
						? "?rotate=" + rotate
						: ""),

				" alt=\"%h\"",
				media.getFilename (),

				">");

		} else {

			return

				stringFormat (
					"(unable to display %h)",
					mimeType);

		}

	}

	@Override
	public
	String mediaContent (
			MediaRec media) {

		return mediaContent (
			media,
			null);

	}

	@Override
	public
	String mediaUrlScaled (
			@NonNull MediaRec media,
			@NonNull Integer width,
			@NonNull Integer height) {

		MediaTypeRec mediaType =
			media.getMediaType ();

		if (! mediaLogic.isImage (media)) {

			throw new RuntimeException (
				stringFormat (
					"Unable to created scaled url for %s",
					mediaType.getMimeType ()));

		}

		return stringFormat (
			"%s",
			mediaHelper.getDefaultContextPath (
				media),
			"/media.imageScale",
			"?width=%u",
			width,
			"&height=%u",
			height);

	}

	@Override
	public
	String mediaContentScaled (
			@NonNull MediaRec media,
			@NonNull Integer width,
			@NonNull Integer height) {

		MediaTypeRec mediaType =
			media.getMediaType ();

		if (! mediaLogic.isImage (media)) {

			return stringFormat (
				"(unable to display %s)",
				mediaType.getMimeType ());

		}

		return stringFormat (
			"<img src=\"%h\">",
			mediaUrlScaled (
				media,
				width,
				height));

	}

	@Override
	public
	String mediaThumb100 (
			MediaRec media,
			String rotate) {

		if (
			mediaLogic.isText (
				media)
		) {

			return

				stringFormat (
					"%h",
					bytesToString (
						media.getContent ().getData (),
						media.getEncoding ()));

		} else if (
			media.getThumb100Content () == null
		) {

			return

				stringFormat (
					"[%h]",
					media.getMediaType ().getMimeType ());

		} else {

			return

				stringFormat (
					"<img",

					" src=\"%h\"",
					stringFormat (
						"%s",
						mediaHelper.getDefaultContextPath (media),
						"/media.thumb100",
						rotate != null
							? "?rotate=" + rotate
							: ""),

					" alt=\"%h\"",
					media.getFilename (),

					">\n");

		}

	}

	@Override
	public
	String mediaThumb100 (
			MediaRec media) {

		return mediaThumb100 (
			media,
			null);

	}

	@Override
	public
	String mediaThumb100OrText (
			MediaRec media) {

		if (equal (
				media.getMediaType ().getMimeType (),
				"text/plain")) {

			return

				stringFormat (
					"%h",
					bytesToString (
						media.getContent ().getData (),
						media.getEncoding ()));

		} else {

			return mediaThumb100 (
				media);

		}

	}

	@Override
	public
	String mediaThumb100Rot90 (
			MediaRec media) {

		if (media.getThumb100Content () == null) {

			return stringFormat (

				"[%h]",
				media.getMediaType ().getMimeType ());

		} else {

			return stringFormat (

				"<img",

				" src=\"%h\"",
				stringFormat (
					"%s",
					mediaHelper.getDefaultContextPath (media),
					"/media.thumb100Rot90"),

				" alt=\"%h\"",
				media.getFilename (),

				">\n");

		}

	}

	@Override
	public
	String mediaThumb32Url (
			MediaRec media) {

		if (media.getThumb32Content () == null) {

			return stringFormat (
				"[%h]",
				media.getMediaType ().getMimeType ());

		} else {

			return stringFormat (

				"<img",

				" src=\"%h\"",
				stringFormat (
					"%s",
					mediaHelper.getDefaultContextPath (media),
					"/media.thumb32"),

				" alt=\"%h\"",
				media.getFilename (),

				">");

		}

	}

	@Override
	public
	String mediaThumb32 (
			MediaRec media) {

		return mediaThumb32Url (
			media);

	}

	@Override
	public
	String mediaThumb32OrText (
			MediaRec media) {

		if (equal (
				media.getMediaType ().getMimeType (),
				"text/plain")) {

			return stringFormat (
				"%h",
				bytesToString (
					media.getContent ().getData (),
					media.getEncoding ()));

		} else {

			return mediaThumb32 (
				media);

		}

	}

	Map<String,String> iconNames =
		ImmutableMap.<String,String>builder ()
			.put ("text/plain", "txt.png")
			.put ("application/smil", "xml6.png")
			.build ();

}
