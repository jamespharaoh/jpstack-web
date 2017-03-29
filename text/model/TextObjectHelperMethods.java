package wbs.platform.text.model;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormatArray;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;

public
interface TextObjectHelperMethods {

	TextRec findOrCreate (
			TaskLogger parentTaskLogger,
			String stringValue);

	default
	Optional <TextRec> findOrCreate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Optional <String> stringValue) {

		if (
			optionalIsPresent (
				stringValue)
		) {

			return optionalOf (
				findOrCreate (
					parentTaskLogger,
					optionalGetRequired (
						stringValue)));

		} else {

			return optionalAbsent ();

		}

	}

	default
	TextRec findOrCreateFormat (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String ... arguments) {

		return findOrCreate (
			parentTaskLogger,
			stringFormatArray (
				arguments));

	}

}