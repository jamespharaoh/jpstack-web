package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.emptyStringIfNull;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.FormatWriter;
import wbs.framework.utils.etc.Html;

@PrototypeComponent ("textAreaFormFieldRenderer")
@Accessors (fluent = true)
public
class TextAreaFormFieldRenderer<Container>
	implements FormFieldRenderer<Container,String> {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

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
	FormFieldDataProvider formFieldDataProvider;

	@Getter @Setter
	Record<?> parent;

	// details

	@Getter
	boolean fileUpload = false;

	// implementation

	@Override
	public
	void renderTableCellList (
			FormatWriter out,
			Container container,
			String interfaceValue,
			boolean link) {

		out.writeFormat (
			"<td>%s</td>\n",
			interfaceToHtmlSimple (
				container,
				interfaceValue,
				link));

	}

	@Override
	public
	void renderTableCellProperties (
			FormatWriter out,
			Container container,
			String interfaceValue) {

		out.writeFormat (
			"<td>%s</td>\n",
			interfaceToHtmlComplex (
				container,
				interfaceValue));

	}

	@Override
	public
	void renderTableRow (
			FormatWriter out,
			Container container,
			String interfaceValue) {

		List<String> errors =
			new ArrayList<String> ();

		if (formValuePresent ()) {

			interfaceValue =
				formToInterface (
					errors);

			if (! errors.isEmpty ())
				throw new RuntimeException ();

		}

		out.writeFormat (
			"<tr>\n",
			"<th>%h</th>\n",
			label ());

		renderTableCellProperties (
			out,
			container,
			interfaceValue);

		out.writeFormat (
			"</tr>\n");

	}

	@Override
	public
	void renderFormRow (
			FormatWriter out,
			Container container,
			String interfaceValue) {

		out.writeFormat (
			"<tr>\n",
			"<th>%h</th>\n",
			label (),
			"<td>");

		renderFormInput (
			out,
			container,
			interfaceValue);

		out.writeFormat (
			"</td>\n",
			"</tr>\n");

	}

	@Override
	public
	void renderFormInput (
			FormatWriter out,
			Container container,
			String interfaceValue) {

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
			emptyStringIfNull (
				interfaceValue));

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

			String data;

			if (parent != null) {

				data =
					formFieldDataProvider.getFormFieldData (
						parent);

			} else {

				data =
					formFieldDataProvider.getFormFieldData (
						(Record<?>) container);

			}

			out.writeFormat (
				"<span hidden=\"hidden\"",
				" class=\"parameters-length-list\"",
				data,
				"></span>\n");

			out.writeFormat (
				"<br>\n");

			/*
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
					(Object)
					container;

				MessageTemplateTypeRec messageTemplateType =
					(MessageTemplateTypeRec)
					(Object)
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
	boolean formValuePresent () {

		String paramString =
			requestContext.parameter (name ());

		return paramString != null;

	}

	@Override
	public
	String formToInterface (
			List<String> errors) {

		return requestContext.parameter (
			name ());

	}

	@Override
	public
	String interfaceToHtmlSimple (
			Container container,
			String interfaceValue,
			boolean link) {

		return Html.newlineToBr (
			stringFormat (
				"%h",
				emptyStringIfNull (
					interfaceValue)));

	}

	@Override
	public
	String interfaceToHtmlComplex (
			Container container,
			String interfaceValue) {

		return interfaceToHtmlSimple (
			container,
			interfaceValue,
			true);

	}

}
