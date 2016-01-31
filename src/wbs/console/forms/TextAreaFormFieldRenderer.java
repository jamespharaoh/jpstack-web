package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.successResult;

import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.forms.FormField.FormType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.FormatWriter;
import wbs.framework.utils.etc.Html;

@PrototypeComponent ("textAreaFormFieldRenderer")
@Accessors (fluent = true)
public
class TextAreaFormFieldRenderer<Container,Parent>
	implements FormFieldRenderer<Container,String> {

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
			@NonNull Optional<String> interfaceValue,
			@NonNull FormType formType) {

		if (
			formValuePresent (
				submission)
		) {
			interfaceValue =
				Optional.of (
					formValue (
						submission));
		}

		htmlWriter.writeFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"%h\"",
			name (),
			" value=\"%h\"",
			interfaceValue.or (""),
			">\n");

	}

	@Override
	public
	void renderFormInput (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue,
			@NonNull FormType formType) {

		out.writeFormat (
			"<textarea",
			" id=\"field_%h\"",
			name (),
			" rows=\"%h\"",
			rows (),
			" cols=\"%h\"",
			cols (),
			" name=\"%h\"",
			name ());

		if (charCountFunction != null) {

			String onchange =
				stringFormat (
					"%s (this, document.getElementById ('%j'), %s);",
					charCountFunction,
					"chars_" + name (),
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
					submission)
				? formValue (
					submission)
				: interfaceValue.or (
					""));

		if (charCountFunction != null) {

			out.writeFormat (
				"<br>\n");

			out.writeFormat (
				"<span",
				" id=\"chars_%h\"",
				name (),
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
							throw new RuntimeException ("Message text is invalid");

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
					"Your template has %d characters. ",
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
			@NonNull String indent,
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			@NonNull FormType formType) {

		if (
			in (
				formType,
				FormType.create,
				FormType.perform,
				FormType.search)
		) {

			javascriptWriter.writeFormat (
				"%s$(\"#%j\").val (\"\");\n",
				indent,
				name);

		} else if (
			in (
				formType,
				FormType.update)
		) {

			javascriptWriter.writeFormat (
				"%s$(\"#%j\").val (\"%j\");\n",
				indent,
				name,
				interfaceValue.or (""));

		} else {

			throw new RuntimeException ();

		}

	}

	@Override
	public
	boolean formValuePresent (
			@NonNull FormFieldSubmission submission) {

		return submission.hasParameter (
			name ());

	}

	String formValue (
			@NonNull FormFieldSubmission submission) {

		return submission.parameter (
			name ());

	}

	@Override
	public
	Either<Optional<String>,String> formToInterface (
			@NonNull FormFieldSubmission submission) {

		return successResult (
			Optional.fromNullable (
				submission.parameter (
					name ())));

	}

	@Override
	public
	String interfaceToHtmlSimple (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue,
			boolean link) {

		return Html.newlineToBr (
			stringFormat (
				"%h",
				interfaceValue.or ("")));

	}

	@Override
	public
	String interfaceToHtmlComplex (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue) {

		return interfaceToHtmlSimple (
			container,
			hints,
			interfaceValue,
			true);

	}

}
