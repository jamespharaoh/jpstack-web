package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.StringUtils.stringFormatArray;

import lombok.NonNull;

public
class DebugUtils {

	public static
	void debugFormat (
			@NonNull Object... arguments) {

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
