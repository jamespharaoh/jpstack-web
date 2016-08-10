package wbs.framework.utils.etc;

import lombok.NonNull;

import com.google.common.base.Optional;

public
class NumberUtils {

	public static
	Long parseLongRequired (
			@NonNull String stringValue) {

		try {

			return Long.parseLong (
				stringValue);

		} catch (NumberFormatException numberFormatException) {

			throw new RuntimeNumberFormatException (
				numberFormatException);

		}

	}

	public static
	Optional<Long> parseLong (
			@NonNull String stringValue) {

		try {

			return Optional.of (
				Long.parseLong (
					stringValue));

		} catch (NumberFormatException numberFormatException) {

			return Optional.absent ();

		}

	}

}
