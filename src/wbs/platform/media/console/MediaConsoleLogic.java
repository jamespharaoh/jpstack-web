package wbs.platform.media.console;

import static wbs.utils.string.FormatWriterUtils.currentFormatWriter;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;

import wbs.platform.media.model.MediaRec;

import wbs.utils.string.FormatWriter;

public
interface MediaConsoleLogic {

	String mediaUrl (
			TaskLogger parentTaskLogger,
			MediaRec media);

	String mediaUrlScaled (
			TaskLogger parentTaskLogger,
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
			TaskLogger parentTaskLogger,
			FormatWriter formatWriter,
			MediaRec media,
			String rotate);

	default
	void writeMediaContent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MediaRec media,
			@NonNull String rotate) {

		writeMediaContent (
			parentTaskLogger,
			currentFormatWriter (),
			media,
			rotate);

	}

	default
	void writeMediaContent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull MediaRec media) {

		writeMediaContent (
			parentTaskLogger,
			formatWriter,
			media,
			"");

	}

	default
	void writeMediaContent (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MediaRec media) {

		writeMediaContent (
			parentTaskLogger,
			currentFormatWriter (),
			media,
			"");

	}

	void writeMediaContentScaled (
			TaskLogger parentTaskLogger,
			FormatWriter formatWriter,
			MediaRec media,
			Integer width,
			Integer height);

	default
	void writeMediaContentScaled (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MediaRec media,
			@NonNull Integer width,
			@NonNull Integer height) {

		writeMediaContentScaled (
			parentTaskLogger,
			currentFormatWriter (),
			media,
			width,
			height);

	}

	void writeMediaThumb100 (
			TaskLogger parentTaskLogger,
			FormatWriter formatWriter,
			MediaRec media,
			String rotate);

	default
	void writeMediaThumb100 (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MediaRec media,
			@NonNull String rotate) {

		writeMediaThumb100 (
			parentTaskLogger,
			currentFormatWriter (),
			media,
			rotate);

	}

	default
	void writeMediaThumb100 (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull MediaRec media) {

		writeMediaThumb100 (
			parentTaskLogger,
			formatWriter,
			media,
			"");

	}

	default
	void writeMediaThumb100 (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MediaRec media) {

		writeMediaThumb100 (
			parentTaskLogger,
			currentFormatWriter (),
			media,
			"");

	}

	void writeMediaThumb100OrText (
			TaskLogger parentTaskLogger,
			FormatWriter formatWriter,
			MediaRec media);

	default
	void writeMediaThumb100OrText (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MediaRec media) {

		writeMediaThumb100OrText (
			parentTaskLogger,
			currentFormatWriter (),
			media);

	}

	void writeMediaThumb100Rot90 (
			TaskLogger parentTaskLogger,
			FormatWriter formatWriter,
			MediaRec media);

	default
	void writeMediaThumb100Rot90 (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MediaRec media) {

		writeMediaThumb100Rot90 (
			parentTaskLogger,
			currentFormatWriter (),
			media);

	}

	void writeMediaThumb32 (
			TaskLogger parentTaskLogger,
			FormatWriter formatWriter,
			MediaRec media);

	default
	void writeMediaThumb32 (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MediaRec media) {

		writeMediaThumb32 (
			parentTaskLogger,
			currentFormatWriter (),
			media);

	}

	void writeMediaThumb32OrText (
			TaskLogger parentTaskLogger,
			FormatWriter formatWriter,
			MediaRec media);

	default
	void writeMediaThumb32OrText (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MediaRec media) {

		writeMediaThumb32OrText (
			parentTaskLogger,
			currentFormatWriter (),
			media);

	}

}
