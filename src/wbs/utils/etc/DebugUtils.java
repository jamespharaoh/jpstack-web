package wbs.utils.etc;

import static wbs.utils.string.StringUtils.stringFormatArray;

import lombok.NonNull;

public
class DebugUtils {

	public static
	void debugFormat (
			@NonNull String ... arguments) {

		System.err.print (
			"====== ");

		System.err.print (
			Thread.currentThread ().getName ());

		System.err.print (
			" ");

		System.err.print (
			stringFormatArray (
				arguments));

		System.err.print (
			"\n");

	}

}
