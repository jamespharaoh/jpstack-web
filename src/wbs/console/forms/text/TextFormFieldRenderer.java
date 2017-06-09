package wbs.console.forms.text;

import static wbs.utils.collection.MapUtils.mapIsNotEmpty;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringIsEmpty;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormField;
import wbs.console.forms.types.FormFieldRenderer;
import wbs.console.forms.types.FormFieldSubmission;
import wbs.console.forms.types.FormType;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import fj.data.Either;

@PrototypeComponent ("textFormFieldRenderer")
@Accessors (fluent = true)
public
class TextFormFieldRenderer <Container>
	implements FormFieldRenderer <Container, String> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String label;

	@Getter @Setter
	Boolean nullable;

	@Getter @Setter
	Integer size =
		FormField.defaultSize;

	@Getter @Setter
	FormField.Align listAlign =
		FormField.Align.left;

	@Getter @Setter
	FormField.Align propertiesAlign =
		FormField.Align.left;

	@Getter @Setter
	Map <String, String> presets =
		new LinkedHashMap<> ();

	// utilities

	public
	TextFormFieldRenderer<Container> addPreset (
			@NonNull String preset) {

		presets.put (
			preset,
			preset);

		return this;

	}

	// implementation

	@Override
	public
	void renderFormTemporarilyHidden (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> suppliedInterfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		Optional <String> interfaceValue =
			ifThenElse (
				formValuePresent (
					submission,
					formName),
				() -> optionalOf (
					formValue (
						submission,
						formName)),
				() -> suppliedInterfaceValue);

		htmlWriter.writeLineFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"%h.%h\"",
			formName,
			name (),
			" value=\"%h\"",
			interfaceValue.or (""),
			">");

	}

	@Override
	public
	void renderFormInput (
			@NonNull Transaction parentTransaction,
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		out.writeIndent ();

		out.writeFormat (
			"<input",
			" type=\"text\"",
			" id=\"%h.%h\"",
			formName,
			name (),
			" name=\"%h.%h\"",
			formName,
			name (),
			" value=\"%h\"",
			formValuePresent (
					submission,
					formName)
				? formValue (
					submission,
					formName)
				: interfaceValue.or (
					""),
			" size=\"%h\"",
			integerToDecimalString (
				size),
			">");

		if (
			mapIsNotEmpty (
				presets ())
		) {

			out.writeFormat (
				"<br>");

			out.writeNewline ();

			for (
				Map.Entry<String,String> presetEntry
					: presets ().entrySet ()
			) {

				out.writeLineFormat (
					"<input",
					" type=\"button\"",
					" name=\"%h\"",
					stringFormat (
						"%s.%s.%s",
						formName,
						name (),
						presetEntry.getValue ()),
					" onclick=\"%h\"",
					stringFormat (
						"$('%j').val ('%j'); return false",
						stringFormat (
							"#%s\\.%s",
							formName,
							name ()),
						presetEntry.getValue ()),
					" value=\"%h\"",
					presetEntry.getKey (),
					">");

			}

		} else {

			out.writeNewline ();

		}

	}

	@Override
	public
	void renderFormReset (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter javascriptWriter,
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			@NonNull String formName) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderFormReset");

		) {

			javascriptWriter.writeLineFormat (
				"$(\"%j\").val (\"%j\");",
				stringFormat (
					"#%s\\.%s",
					formName,
					name),
				interfaceValue.or (""));

		}

	}

	@Override
	public
	boolean formValuePresent (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		return submission.hasParameter (
			stringFormat (
				"%s.%s",
				formName,
				name ()));

	}

	String formValue (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		return submission.parameter (
			stringFormat (
				"%s.%s",
				formName,
				name ()));

	}

	@Override
	public
	Either <Optional <String>, String> formToInterface (
			@NonNull Transaction parentTransaction,
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"formToInterface");

		) {

			String formValue =
				formValue (
					submission,
					formName);

			if (

				nullable ()

				&& stringIsEmpty (
					formValue)

			) {

				return successResult (
					optionalAbsent ());

			}

			return successResult (
				optionalOf (
					formValue));

		}

	}

	@Override
	public
	void renderHtmlSimple (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue,
			boolean link) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlSimple");

		) {

			htmlWriter.writeLineFormat (
				"%h",
				interfaceValue.or (
					"—"));

		}

	}

	@Override
	public
	void renderHtmlTableCellList (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue,
			@NonNull Boolean link,
			@NonNull Long colspan) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlTableCellList");

		) {

			htmlTableCellOpen (
				formatWriter,
				htmlStyleRuleEntry (
					"text-align",
					listAlign ().name ()),
				htmlColumnSpanAttribute (
					colspan),
				htmlClassAttribute (
					presentInstances (
						htmlClass (
							interfaceValue))));

			renderHtmlSimple (
				transaction,
				formatWriter,
				container,
				hints,
				interfaceValue,
				link);

			htmlTableCellClose (
				formatWriter);

		}

	}

	@Override
	public
	void renderHtmlTableCellProperties (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue,
			@NonNull Boolean link,
			@NonNull Long colspan) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlTableCellProperties");

		) {

			htmlTableCellOpen (
				formatWriter,
				htmlStyleRuleEntry (
					"text-align",
					propertiesAlign ().name ()),
				htmlColumnSpanAttribute (
					colspan));

			renderHtmlSimple (
				transaction,
				formatWriter,
				container,
				hints,
				interfaceValue,
				link);

			htmlTableCellClose (
				formatWriter);

		}

	}

}
