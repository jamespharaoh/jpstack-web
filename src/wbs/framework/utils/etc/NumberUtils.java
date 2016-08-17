package wbs.framework.utils.etc;

import com.google.common.base.Optional;

import lombok.NonNull;

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

	public static
	Optional <Integer> toJavaInteger (
			@NonNull Long integerValue) {

		try {

			return Optional.of (
				Math.toIntExact (
					integerValue));

		} catch (ArithmeticException arithmeticException) {

			return Optional.absent ();

		}

	}

	public static
	int toJavaIntegerRequired (
			@NonNull Long integerValue) {

		return Math.toIntExact (
			integerValue);

	}

	public static
	int toJavaIntegerRequired (
			long integerValue) {

		return Math.toIntExact (
			integerValue);

	}

	public static
	Long fromJavaInteger (
			@NonNull Integer javaIntegerObject) {

		int javaIntegerValue =
			javaIntegerObject;

		long integerValue =
			javaIntegerValue;

		return integerValue;

	}

	public static
	long fromJavaInteger (
			int javaIntegerValue) {

		long integerValue =
			javaIntegerValue;

		return integerValue;

	}

	public static
	long roundToIntegerRequired (
			double originalDoubleValue) {

		double roundedDoubleValue =
			Math.round (
				originalDoubleValue);

		if (
			roundedDoubleValue < Long.MIN_VALUE
			|| roundedDoubleValue > Long.MAX_VALUE
		) {
			throw new ArithmeticException ();
		}

		return (long)
			roundedDoubleValue;

	}

}
