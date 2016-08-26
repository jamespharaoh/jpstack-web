package wbs.framework.codegen;

import static wbs.framework.utils.etc.StringUtils.stringFormatArray;
import static wbs.framework.utils.etc.TypeUtils.classNameFull;

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
