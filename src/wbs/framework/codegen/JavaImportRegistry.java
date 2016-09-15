package wbs.framework.codegen;

import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.string.StringUtils.stringFormatArray;

import lombok.NonNull;

public
interface JavaImportRegistry {

	String register (
			String className);

	default
	String register (
			@NonNull Class <?> classObject) {

		return register (
			classNameFull (
				classObject));

	}

	default
	String registerFormat (
			@NonNull Object ... arguments) {

		return register (
			stringFormatArray (
				arguments));

	}

}
