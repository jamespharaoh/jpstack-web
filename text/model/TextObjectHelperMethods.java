package wbs.platform.text.model;

import static wbs.utils.string.StringUtils.stringFormatArray;

import lombok.NonNull;

public
interface TextObjectHelperMethods {

	TextRec findOrCreate (
			String stringValue);

	default
	TextRec findOrCreateFormat (
			@NonNull String ... arguments) {

		return findOrCreate (
			stringFormatArray (
				arguments));

	}

	TextRec findOrCreateMapNull (
			String stringValue);

}