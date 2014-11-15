package wbs.platform.media.console;

import wbs.platform.media.model.MediaRec;

public
interface MediaConsoleLogic {

	String mediaUrl (
			MediaRec media);

	String mediaContent (
			MediaRec media,
			String rotate);

	String mediaContent (
			MediaRec media);

	String mediaThumb100 (
			MediaRec media,
			String rotate);

	String mediaThumb100 (
			MediaRec media);

	String mediaThumb100OrText (
			MediaRec media);

	String mediaThumb100Rot90 (
			MediaRec media);

	String mediaThumb32Url (
			MediaRec media);

	String mediaThumb32 (
			MediaRec media);

	String mediaThumb32OrText (
			MediaRec media);

}
