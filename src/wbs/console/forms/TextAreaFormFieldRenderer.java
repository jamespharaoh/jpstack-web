package wbs.console.forms;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

import fj.data.Either;
import wbs.web.utils.HtmlUtils;

@PrototypeComponent ("textAreaFormFieldRenderer")
@Accessors (fluent = true)
public
class TextAreaFormFieldRenderer <Container, Parent>
	implements FormFieldRenderer <Container, String> {

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String label;

	@Getter @Setter
	Boolean nullable;

	@Getter @Setter
	Integer rows;

	@Getter @Setter
	Integer cols;

	@Getter @Setter
	String charCountFunction;

	@Getter @Setter
	String charCountData;

	@Getter @Setter
	FormFieldDataProvider<Container,Parent> formFieldDataProvider;

	@Getter @Setter
	Parent parent;

	// implementation

	@Override
	public
	void renderFormTemporarilyHidden (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
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

		htmlWriter.writeFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"%h.%h\"",
			formName,
			name (),
			" value=\"%h\"",
			interfaceValue.or (""),
			">\n");

	}

	@Override
	public
	void renderFormInput (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		out.writeFormat (
			"<textarea",
			" id=\"%h.%h\"",
			formName,
			name (),
			" rows=\"%h\"",
			integerToDecimalString (
				rows ()),
			" cols=\"%h\"",
			integerToDecimalString (
				cols ()),
			" name=\"%h.%h\"",
			formName,
			name ());

		if (charCountFunction != null) {

			String onchange =
				stringFormat (
					"%s (this, document.getElementById ('%j'), %s);",
					charCountFunction,
					stringFormat (
						"%s.%s:chars",
						formName,
						name ()),
					ifNull (
						charCountData,
						"undefined"));

			out.writeFormat (
				" onkeyup=\"%h\"",
				onchange,
				" onfocus=\"%h\"",
				onchange);

		}

		out.writeFormat (
			">%h</textarea>",
			formValuePresent (
					submission,
					formName)
				? formValue (
					submission,
					formName)
				: interfaceValue.or (
					""));

		if (charCountFunction != null) {

			out.writeFormat (
				"<br>\n");

			out.writeFormat (
				"<span",
				" id=\"%h\"",
				stringFormat (
					"%s.%s:chars",
					formName,
					name ()),
				">&nbsp;</span>");

		}

		if (formFieldDataProvider != null) {

			/*
			String data =
				formFieldDataProvider.getFormFieldDataForObject (
					container);

			out.writeFormat (
				"<span",
				" hidden=\"hidden\"",
				" class=\"parameters-length-list\"",
				data,
				"></span>\n");

			out.writeFormat (
				"<br>\n");

			// parameters data

			String[] tokens =
				data.split ("&");

			Map<String,String> dataMap =
				new TreeMap<String,String> ();

			for (Integer i = 0; i < tokens.length; i++) {

				String[] parameter =
					tokens[i].split("=");

				dataMap.put (
					parameter [0],
					parameter [1]);

			}

			// message and charset

			String message;
			MessageTemplateTypeCharset charset;

			if (parent == null) {

				message =
					((MessageTemplateTypeRec) container)
						.getDefaultValue();

				charset =
					((MessageTemplateTypeRec) container)
						.getCharset();

			} else {

				// if the type has a defined value, we get it

				MessageTemplateSetRec messageTemplateSet =
					(MessageTemplateSetRec)
					container;

				MessageTemplateTypeRec messageTemplateType =
					(MessageTemplateTypeRec)
					parent;

				MessageTemplateValueRec messageTemplateValue =
					messageTemplateSet.getMessageTemplateValues ().get (
						messageTemplateType);

				if (messageTemplateValue == null) {

					message =
						messageTemplateType.getDefaultValue ();

				} else {

					message =
						messageTemplateValue.getStringValue ();

				}

				charset =
					messageTemplateType.getCharset ();

			}

			// length of non variable parts

			Integer messageLength = 0;

			if (message != null) {

				String[] parts =
					message.split("\\{(.*?)\\}");

				for (int i = 0; i < parts.length; i++) {

					// length of special chars if gsm encoding

					if (charset == MessageTemplateTypeCharset.gsm) {

						if (! Gsm.isGsm (parts[i]))
							throw new RuntimeException (
								"Message text is invalid");

						messageLength +=
							Gsm.length (parts[i]);

					}
					else {
						messageLength +=
								parts[i].length();
					}

				}

				// length of the parameters

				Pattern regExp =
					Pattern.compile ("\\{(.*?)\\}");

				Matcher matcher =
					regExp.matcher (message);

				while (matcher.find ()) {

					String parameterName =
						matcher.group (1);

						messageLength +=
							Integer.parseInt (
								dataMap.get (parameterName));

				}

			}

			out.write (
				stringFormat (
					"<span",
					" class=\"templatechars\"",
					">",
					"Your template has %s characters. ",
					messageLength,
					"(Min. Length: %s - ",
					dataMap.get("minimumTemplateLength"),
					"Max. Length: %s)",
					dataMap.get("maximumTemplateLength"),
					"</span>"));

			*/

		}

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull Container container,
			@NonNull Optional <String> interfaceValue,
			@NonNull String formName) {

		javascriptWriter.writeFormat (
			"$(\"%j\").val (\"%j\");",
			stringFormat (
				"#%s\\.%s",
				formName,
				name),
			interfaceValue.or (""));

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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		return successResult (
			optionalFromNullable (
				submission.parameter (
					stringFormat (
						"%s.%s",
						formName,
						name ()))));

	}

	@Override
	public
	void renderHtmlSimple (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue,
			boolean link) {

		htmlWriter.writeFormat (
			"%s",
			HtmlUtils.newlineToBr (
			stringFormat (
				"%h",
				interfaceValue.or (""))));

	}

}
