package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.StringUtils.stringFormatArray;
import lombok.NonNull;

public
class DebugUtils {

	public static
	void debugFormat (
			@NonNull String... arguments) {

		System.out.print (
			"====== ");

		System.out.print (
			Thread.currentThread ().getName ());

		System.out.print (
			" ");

		System.out.print (
			stringFormatArray (
				arguments));

		System.out.print (
			"\n");

	}

}
