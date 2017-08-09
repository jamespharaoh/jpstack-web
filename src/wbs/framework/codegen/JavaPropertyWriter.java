package wbs.framework.codegen;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.lang.reflect.Method;
import java.util.function.Function;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("javaPropertyWriter")
public
class JavaPropertyWriter
	implements JavaBlockWriter {

	// properties

	String thisClassName;
	Function <JavaImportRegistry, String> typeName;
	Function <JavaImportRegistry, String> setterTypeName;
	String setterConversion;
	String propertyName;
	String setUpdatedFieldName;
	Function <JavaImportRegistry, String> defaultValue;

	// this class name

	public
	JavaPropertyWriter thisClassName (
			@NonNull String thisClassName) {

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
	JavaPropertyWriter thisClassNameFormat (
			@NonNull String ... arguments) {

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
			@NonNull String ... arguments) {

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
			@NonNull String ... arguments) {

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
			@NonNull String ... arguments) {

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
			@NonNull String ... arguments) {

		return propertyName (
			stringFormatArray (
				arguments));

	}

	// set updated field name

	public
	JavaPropertyWriter setUpdatedFieldName (
			@NonNull String setUpdatedFieldName) {

		if (
			isNotNull (
				this.setUpdatedFieldName)
		) {
			throw new IllegalStateException ();
		}

		this.setUpdatedFieldName =
			setUpdatedFieldName;

		return this;

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
			@NonNull String ... arguments) {

		return defaultValue (
			stringFormatArray (
				arguments));

	}

	// implementation

	@Override
	public
	void writeBlock (
			@NonNull TaskLogger parentTaskLogger,
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

		formatWriter.writeNewline ();

		// write setter

		formatWriter.writeLineFormat (
			"public");

		formatWriter.writeLineFormat (
			"%s set%s (",
			thisClassName,
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

		formatWriter.increaseIndent ();

		formatWriter.writeNewline ();

		formatWriter.writeLineFormat (
			"this.%s =",
			propertyName);

		if (
			isNotNull (
				setterConversion)
		) {

			formatWriter.writeLineFormat (
				"\t%s (",
				setterConversion);

			formatWriter.writeLineFormat (
				"\t\t%s);",
				propertyName);

		} else {

			formatWriter.writeLineFormat (
				"\t%s;",
				propertyName);

		}

		formatWriter.writeNewline ();

		if (
			isNotNull (
				setUpdatedFieldName)
		) {

			formatWriter.writeLineFormat (
				"%s = true;",
				setUpdatedFieldName);

			formatWriter.writeNewline ();

		}

		formatWriter.writeLineFormat (
			"return this;");

		formatWriter.writeNewline ();

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		formatWriter.writeNewline ();

	}

}
