package wbs.framework.codegen;

import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.StringUtils.stringFormatArray;

import java.lang.reflect.Method;

import lombok.NonNull;
import wbs.framework.utils.formatwriter.FormatWriter;

public
class JavaPropertyWriter {

	// properties

	String thisClassName;
	String typeName;
	String setterTypeName;
	String setterConversion;
	String propertyName;
	String defaultValue;

	public
	JavaPropertyWriter thisClassNameFormat (
			Object... arguments) {

		if (thisClassName != null) {
			throw new IllegalStateException ();
		}

		thisClassName =
			stringFormatArray (
				arguments);

		return this;

	}

	public
	JavaPropertyWriter typeNameFormat (
			Object... arguments) {

		if (typeName != null) {
			throw new IllegalStateException ();
		}

		typeName =
			stringFormatArray (
				arguments);

		return this;

	}

	public
	JavaPropertyWriter setterTypeNameFormat (
			@NonNull Object... arguments) {

		if (setterTypeName != null) {
			throw new IllegalStateException ();
		}

		setterTypeName =
			stringFormatArray (
				arguments);

		return this;

	}

	public
	JavaPropertyWriter setterConversionFormat (
			@NonNull Object... arguments) {

		if (setterConversion != null) {
			throw new IllegalStateException ();
		}

		setterConversion =
			stringFormatArray (
				arguments);

		return this;

	}

	public
	JavaPropertyWriter setterConversion (
			@NonNull Method method) {

		return setterConversionFormat (
			"%s.%s",
			method.getDeclaringClass ().getName (),
			method.getName ());

	}

	public
	JavaPropertyWriter propertyNameFormat (
			Object... arguments) {

		if (propertyName != null) {
			throw new IllegalStateException ();
		}

		propertyName =
			stringFormatArray (
				arguments);

		return this;

	}

	public
	JavaPropertyWriter defaultValueFormat (
			Object... arguments) {

		if (defaultValue != null) {
			throw new IllegalStateException ();
		}

		defaultValue =
			stringFormatArray (
				arguments);

		return this;

	}

	public
	void write (
			FormatWriter javaWriter,
			String indent) {

		// write member variable

		if (defaultValue == null) {

			javaWriter.writeFormat (
				"%s%s %s;\n",
				indent,
				typeName,
				propertyName);

		} else {

			javaWriter.writeFormat (
				"%s%s %s =\n",
				indent,
				typeName,
				propertyName);

			javaWriter.writeFormat (
				"%s\t%s;\n",
				indent,
				defaultValue);

		}

		javaWriter.writeFormat (
			"\n");

		// write getter

		javaWriter.writeFormat (
			"%spublic\n",
			indent);

		javaWriter.writeFormat (
			"%s%s get%s () {\n",
			indent,
			typeName,
			capitalise (
				propertyName));

		javaWriter.writeFormat (
			"%s\treturn %s;\n",
			indent,
			propertyName);

		javaWriter.writeFormat (
			"%s}\n",
			indent);

		javaWriter.writeFormat (
			"\n");

		// write setter

		javaWriter.writeFormat (
			"%spublic\n",
			indent);

		javaWriter.writeFormat (
			"%s%s set%s (\n",
			indent,
			thisClassName,
			capitalise (
				propertyName));

		javaWriter.writeFormat (
			"%s\t\t%s %s) {\n",
			indent,
			ifNull (
				setterTypeName,
				typeName),
			propertyName);

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"%s\tthis.%s =\n",
			indent,
			propertyName);

		if (
			isNotNull (
				setterConversion)
		) {

			javaWriter.writeFormat (
				"%s\t\t%s (\n",
				indent,
				setterConversion);

			javaWriter.writeFormat (
				"%s\t\t\t%s);\n",
				indent,
				propertyName);

		} else {

			javaWriter.writeFormat (
				"%s\t\t%s;\n",
				indent,
				propertyName);

		}

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"%s\treturn this;\n",
			indent);

		javaWriter.writeFormat (
			"\n");

		javaWriter.writeFormat (
			"%s}\n",
			indent);

		javaWriter.writeFormat (
			"\n");

	}

}
