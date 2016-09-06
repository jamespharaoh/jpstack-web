package wbs.framework.codegen;

import static wbs.framework.utils.etc.LogicUtils.ifThenElse;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.stringFormatArray;

import java.lang.reflect.Method;
import java.util.function.Function;

import lombok.NonNull;

import wbs.framework.utils.formatwriter.FormatWriter;

public
class JavaPropertyWriter
	implements JavaBlockWriter {

	// properties

	Function <JavaImportRegistry, String> thisClassName;
	Function <JavaImportRegistry, String> typeName;
	Function <JavaImportRegistry, String> setterTypeName;
	String setterConversion;
	String propertyName;
	Function <JavaImportRegistry, String> defaultValue;

	// this class name

	public
	JavaPropertyWriter thisClassName (
			@NonNull Function <JavaImportRegistry, String> thisClassName) {

		if (
			isNotNull (
				this.thisClassName)
		) {
			throw new IllegalStateException ();
		}

		this.thisClassName =
			thisClassName;

		return this;

	}

	public
	JavaPropertyWriter thisClassName (
			@NonNull String thisClassName) {

		return thisClassName (
			imports ->
				imports.register (
					thisClassName));

	}

	public
	JavaPropertyWriter thisClassNameFormat (
			@NonNull Object ... arguments) {

		return thisClassName (
			stringFormatArray (
				arguments));

	}

	public
	JavaPropertyWriter thisClass (
			@NonNull Class <?> thisClass) {

		return thisClassName (
			thisClass.getName ());

	}

	// type name

	public
	JavaPropertyWriter typeName (
			@NonNull Function <JavaImportRegistry, String> typeName) {

		if (
			isNotNull (
				this.typeName)
		) {
			throw new IllegalStateException ();
		}

		this.typeName =
			typeName;

		return this;

	}

	public
	JavaPropertyWriter typeName (
			@NonNull String typeName) {

		return typeName (
			imports ->
				imports.register (
					typeName));

	}

	public
	JavaPropertyWriter typeNameFormat (
			@NonNull Object ... arguments) {

		return typeName (
			stringFormatArray (
				arguments));

	}

	public
	JavaPropertyWriter typeClass (
			@NonNull Class <?> typeClass) {

		return typeName (
			typeClass.getName ());

	}

	// setter type name

	public
	JavaPropertyWriter setterTypeName (
			@NonNull Function <JavaImportRegistry, String> setterTypeName) {

		if (
			isNotNull (
				this.setterTypeName)
		) {
			throw new IllegalStateException ();
		}

		this.setterTypeName =
			setterTypeName;

		return this;

	}

	public
	JavaPropertyWriter setterTypeName (
			@NonNull String setterTypeName) {

		return setterTypeName (
			imports ->
				imports.register (
					setterTypeName));

	}

	public
	JavaPropertyWriter setterTypeNameFormat (
			@NonNull Object ... arguments) {

		return setterTypeName (
			stringFormatArray (
				arguments));

	}

	public
	JavaPropertyWriter setterTypeClass (
			@NonNull Class <?> setterTypeClass) {

		return setterTypeName (
			setterTypeClass.getName ());

	}

	// setter conversation

	public
	JavaPropertyWriter setterConversionFormat (
			@NonNull Object ... arguments) {

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

	// property name

	public
	JavaPropertyWriter propertyName (
			@NonNull String propertyName) {

		if (
			isNotNull (
				this.propertyName)
		) {
			throw new IllegalStateException ();
		}

		this.propertyName =
			propertyName;

		return this;

	}

	public
	JavaPropertyWriter propertyNameFormat (
			@NonNull Object ... arguments) {

		return propertyName (
			stringFormatArray (
				arguments));

	}

	// default value

	public
	JavaPropertyWriter defaultValue (
			@NonNull Function <JavaImportRegistry, String> defaultValue) {

		if (
			isNotNull (
				this.defaultValue)
		) {
			throw new IllegalStateException ();
		}

		this.defaultValue =
			defaultValue;

		return this;

	}

	public
	JavaPropertyWriter defaultValue (
			@NonNull String defaultValue) {

		return defaultValue (
			imports ->
				defaultValue);

	}

	public
	JavaPropertyWriter defaultValueFormat (
			@NonNull Object ... arguments) {

		return defaultValue (
			stringFormatArray (
				arguments));

	}

	// implementation

	@Override
	public
	void writeBlock (
			@NonNull JavaImportRegistry imports,
			@NonNull FormatWriter formatWriter) {

		// write member variable

		if (defaultValue == null) {

			formatWriter.writeLineFormat (
				"%s %s;",
				typeName.apply (
					imports),
				propertyName);

		} else {

			formatWriter.writeLineFormat (
				"%s %s =",
				typeName.apply (
					imports),
				propertyName);

			formatWriter.writeLineFormat (
				"\t%s;",
				defaultValue.apply (
					imports));

		}

		formatWriter.writeNewline ();

		// write getter

		formatWriter.writeLineFormat (
			"public");

		formatWriter.writeLineFormat (
			"%s get%s () {",
			typeName.apply (
				imports),
			capitalise (
				propertyName));

		formatWriter.writeLineFormat (
			"\treturn %s;",
			propertyName);

		formatWriter.writeLineFormat (
			"}");

		formatWriter.writeLineFormat ();

		// write setter

		formatWriter.writeLineFormat (
			"public");

		formatWriter.writeLineFormat (
			"%s set%s (",
			thisClassName.apply (
				imports),
			capitalise (
				propertyName));

		formatWriter.writeLineFormat (
			"\t\t%s %s) {",
			ifThenElse (
				isNotNull (
					setterTypeName),
				() -> setterTypeName.apply (
					imports),
				() -> typeName.apply (
					imports)),
			propertyName);

		formatWriter.writeNewline ();

		formatWriter.writeLineFormat (
			"\tthis.%s =",
			propertyName);

		if (
			isNotNull (
				setterConversion)
		) {

			formatWriter.writeLineFormat (
				"\t\t%s (",
				setterConversion);

			formatWriter.writeLineFormat (
				"\t\t\t%s);",
				propertyName);

		} else {

			formatWriter.writeLineFormat (
				"\t\t%s;",
				propertyName);

		}

		formatWriter.writeNewline ();

		formatWriter.writeLineFormat (
			"\treturn this;");

		formatWriter.writeNewline ();

		formatWriter.writeLineFormat (
			"}");

		formatWriter.writeNewline ();

	}

}
