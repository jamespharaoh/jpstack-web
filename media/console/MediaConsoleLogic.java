package wbs.platform.media.console;

import static wbs.utils.string.FormatWriterUtils.currentFormatWriter;

import lombok.NonNull;

import wbs.framework.database.Transaction;

import wbs.platform.media.model.MediaRec;

import wbs.utils.string.FormatWriter;

public
interface MediaConsoleLogic {

	String mediaUrl (
			Transaction parentTransaction,
			MediaRec media);

	String mediaUrlScaled (
			Transaction parentTransaction,
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
			Transaction parentTransaction,
			FormatWriter formatWriter,
			MediaRec media,
			String rotate);

	default
	void writeMediaContent (
			@NonNull Transaction parentTransaction,
			@NonNull MediaRec media,
			@NonNull String rotate) {

		writeMediaContent (
			parentTransaction,
			currentFormatWriter (),
			media,
			rotate);

	}

	default
	void writeMediaContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull MediaRec media) {

		writeMediaContent (
			parentTransaction,
			formatWriter,
			media,
			"");

	}

	default
	void writeMediaContent (
			@NonNull Transaction parentTransaction,
			@NonNull MediaRec media) {

		writeMediaContent (
			parentTransaction,
			currentFormatWriter (),
			media,
			"");

	}

	void writeMediaContentScaled (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			MediaRec media,
			Integer width,
			Integer height);

	default
	void writeMediaContentScaled (
			@NonNull Transaction parentTransaction,
			@NonNull MediaRec media,
			@NonNull Integer width,
			@NonNull Integer height) {

		writeMediaContentScaled (
			parentTransaction,
			currentFormatWriter (),
			media,
			width,
			height);

	}

	void writeMediaThumb100 (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			MediaRec media,
			String rotate);

	default
	void writeMediaThumb100 (
			@NonNull Transaction parentTransaction,
			@NonNull MediaRec media,
			@NonNull String rotate) {

		writeMediaThumb100 (
			parentTransaction,
			currentFormatWriter (),
			media,
			rotate);

	}

	default
	void writeMediaThumb100 (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull MediaRec media) {

		writeMediaThumb100 (
			parentTransaction,
			formatWriter,
			media,
			"");

	}

	default
	void writeMediaThumb100 (
			@NonNull Transaction parentTransaction,
			@NonNull MediaRec media) {

		writeMediaThumb100 (
			parentTransaction,
			currentFormatWriter (),
			media,
			"");

	}

	void writeMediaThumb100OrText (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			MediaRec media);

	default
	void writeMediaThumb100OrText (
			@NonNull Transaction parentTransaction,
			@NonNull MediaRec media) {

		writeMediaThumb100OrText (
			parentTransaction,
			currentFormatWriter (),
			media);

	}

	void writeMediaThumb100Rot90 (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			MediaRec media);

	default
	void writeMediaThumb100Rot90 (
			@NonNull Transaction parentTransaction,
			@NonNull MediaRec media) {

		writeMediaThumb100Rot90 (
			parentTransaction,
			currentFormatWriter (),
			media);

	}

	void writeMediaThumb32 (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			MediaRec media);

	default
	void writeMediaThumb32 (
			@NonNull Transaction parentTransaction,
			@NonNull MediaRec media) {

		writeMediaThumb32 (
			parentTransaction,
			currentFormatWriter (),
			media);

	}

	void writeMediaThumb32OrText (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			MediaRec media);

	default
	void writeMediaThumb32OrText (
			@NonNull Transaction parentTransaction,
			@NonNull MediaRec media) {

		writeMediaThumb32OrText (
			parentTransaction,
			currentFormatWriter (),
			media);

	}

}
