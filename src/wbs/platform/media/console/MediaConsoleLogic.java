package wbs.platform.media.console;

import static wbs.utils.string.FormatWriterUtils.currentFormatWriter;

import lombok.NonNull;

import wbs.platform.media.model.MediaRec;
import wbs.utils.string.FormatWriter;

public
interface MediaConsoleLogic {

	String mediaUrl (
			MediaRec media);

	String mediaUrlScaled (
			MediaRec media,
			Integer width,
			Integer height);

	/*
	String mediaThumb100Url (
			MediaRec media);

	String mediaThumb32Url (
			MediaRec media);
	*/

	void writeMediaContent (
			FormatWriter formatWriter,
			MediaRec media,
			String rotate);

	default
	void writeMediaContent (
			@NonNull MediaRec media,
			@NonNull String rotate) {

		writeMediaContent (
			currentFormatWriter (),
			media,
			rotate);

	}

	default
	void writeMediaContent (
			@NonNull FormatWriter formatWriter,
			@NonNull MediaRec media) {

		writeMediaContent (
			formatWriter,
			media,
			"");

	}

	default
	void writeMediaContent (
			@NonNull MediaRec media) {

		writeMediaContent (
			currentFormatWriter (),
			media,
			"");

	}

	void writeMediaContentScaled (
			FormatWriter formatWriter,
			MediaRec media,
			Integer width,
			Integer height);

	default
	void writeMediaContentScaled (
			@NonNull MediaRec media,
			@NonNull Integer width,
			@NonNull Integer height) {

		writeMediaContentScaled (
			currentFormatWriter (),
			media,
			width,
			height);

	}

	void writeMediaThumb100 (
			FormatWriter formatWriter,
			MediaRec media,
			String rotate);

	default
	void writeMediaThumb100 (
			@NonNull MediaRec media,
			@NonNull String rotate) {

		writeMediaThumb100 (
			currentFormatWriter (),
			media,
			rotate);

	}

	default
	void writeMediaThumb100 (
			@NonNull FormatWriter formatWriter,
			@NonNull MediaRec media) {

		writeMediaThumb100 (
			formatWriter,
			media,
			"");

	}

	default
	void writeMediaThumb100 (
			@NonNull MediaRec media) {

		writeMediaThumb100 (
			currentFormatWriter (),
			media,
			"");

	}

	void writeMediaThumb100OrText (
			FormatWriter formatWriter,
			MediaRec media);

	default
	void writeMediaThumb100OrText (
			@NonNull MediaRec media) {

		writeMediaThumb100OrText (
			currentFormatWriter (),
			media);

	}

	void writeMediaThumb100Rot90 (
			FormatWriter formatWriter,
			MediaRec media);

	default
	void writeMediaThumb100Rot90 (
			@NonNull MediaRec media) {

		writeMediaThumb100Rot90 (
			currentFormatWriter (),
			media);

	}

	void writeMediaThumb32 (
			FormatWriter formatWriter,
			MediaRec media);

	default
	void writeMediaThumb32 (
			@NonNull MediaRec media) {

		writeMediaThumb32 (
			currentFormatWriter (),
			media);

	}

	void writeMediaThumb32OrText (
			FormatWriter formatWriter,
			MediaRec media);

	default
	void writeMediaThumb32OrText (
			@NonNull MediaRec media) {

		writeMediaThumb32OrText (
			currentFormatWriter (),
			media);

	}

}
