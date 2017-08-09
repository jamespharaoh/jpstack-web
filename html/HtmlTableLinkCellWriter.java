package wbs.console.html;

import static wbs.utils.collection.MapUtils.mapContainsKey;
import static wbs.utils.collection.MapUtils.mapPutOrThrowIllegalStateException;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlUtils.htmlWriteAttributesFromMap;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("htmlTableLinkCellWriter")
public
class HtmlTableLinkCellWriter {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// properties

	Optional <String> nameOptional =
		optionalAbsent ();

	Optional <String> valueOptional =
		optionalAbsent ();

	Optional <String> labelOptional =
		optionalAbsent ();

	Optional <Boolean> selectedOptional =
		optionalAbsent ();

	Map <String, String> tableCellExtra =
		new LinkedHashMap<> ();

	Map <String, String> radioInputExtra =
		new LinkedHashMap<> ();

	// property setters

	public
	HtmlTableLinkCellWriter name (
			@NonNull String name) {

		if (
			optionalIsPresent (
				nameOptional)
		) {
			throw new IllegalStateException ();
		}

		nameOptional =
			optionalOf (
				name);

		return this;

	}

	public
	HtmlTableLinkCellWriter value (
			@NonNull String value) {

		if (
			optionalIsPresent (
				valueOptional)
		) {
			throw new IllegalStateException ();
		}

		valueOptional =
			optionalOf (
				value);

		return this;

	}

	public
	HtmlTableLinkCellWriter label (
			@NonNull String label) {

		if (
			optionalIsPresent (
				labelOptional)
		) {
			throw new IllegalStateException ();
		}

		labelOptional =
			optionalOf (
				label);

		return this;

	}

	public
	HtmlTableLinkCellWriter selected (
			@NonNull Boolean selected) {

		if (
			optionalIsPresent (
				selectedOptional)
		) {
			throw new IllegalStateException ();
		}

		selectedOptional =
			optionalOf (
				selected);

		return this;

	}

	public
	HtmlTableLinkCellWriter addTableCellExtra (
			@NonNull String name,
			@NonNull String value) {

		if (
			mapContainsKey (
				tableCellExtra,
				name)
		) {
			throw new IllegalStateException ();
		}

		tableCellExtra.put (
			name,
			value);

		return this;

	}

	public
	HtmlTableLinkCellWriter addRadioInputExtra (
			@NonNull String name,
			@NonNull String value) {

		mapPutOrThrowIllegalStateException (
			radioInputExtra,
			name,
			value);

		return this;

	}

	// implementation

	public
	void write (
			@NonNull FormatWriter formatWriter) {

		String nameValue =
			nameOptional.get () + "_" + valueOptional.get ();

		// open outer table cell

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<td");

		formatWriter.writeFormat (
			" id=\"%h\"",
			nameValue + "_td");

		formatWriter.writeFormat (
			" class=\"%h\"",
			selectedOptional.get ()
				? "selected"
				: "unselected");

		formatWriter.writeFormat (
			" style=\"cursor: pointer;\"");

		formatWriter.writeFormat (
			" onclick=\"%h\"",
			stringFormat (
				"tdcheck_td ('%j')",
				nameValue));

		formatWriter.writeFormat (
			" onmouseover=\"%h\"",
			stringFormat (
				"tdcheck_focus ('%j')",
				nameValue));

		formatWriter.writeFormat (
			" onmouseout=\"%h\"",
			stringFormat (
				"tdcheck_update ('%j')",
				nameValue));

		htmlWriteAttributesFromMap (
			formatWriter,
			tableCellExtra);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

		// open table

		formatWriter.writeLineFormat (
			"<table",
			" class=\"layout\"",
			" border=\"0\"",
			" cellspacing=\"0\"",
			" cellpadding=\"0\"");

		formatWriter.increaseIndent ();

		// open table row

		formatWriter.writeLineFormat (
			"<tr>");

		formatWriter.increaseIndent ();

		// wtite input cell

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<td><input");

		formatWriter.writeFormat (
			" type=\"radio\"");

		formatWriter.writeFormat (
			" id=\"%h\"",
			nameValue);

		formatWriter.writeFormat (
			" name=\"%h\"",
			nameOptional.get ());

		formatWriter.writeFormat (
			" onfocus=\"%h\"",
			stringFormat (
				"tdcheck_focus ('%j')",
				nameValue));

		formatWriter.writeFormat (
			" onblur=\"%h\"",
			stringFormat (
				"tdcheck_update ('%j')",
				nameValue));

		formatWriter.writeFormat (
			" onclick=\"%h\"",
			stringFormat (
				"tdcheck_checkbox ('%j', event)",
				nameValue));

		formatWriter.writeFormat (
			" value=\"%h\"",
			valueOptional.get ());

		if (selectedOptional.get ()) {

			formatWriter.writeFormat (
				" selected");

		}

		htmlWriteAttributesFromMap (
			formatWriter,
			radioInputExtra);

		formatWriter.writeFormat (
			"></td>");

		// write separator cell

		formatWriter.writeLineFormat (
			"<td>&nbsp;</td>");

		// write label cell

		formatWriter.writeLineFormat (
			"<td>%h</td>",
			labelOptional.get ());

		// close table row

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</tr>");

		// close table

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</table>");

		// close outer table cell

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</td>");

		// add this to the list of script bits still to do

		requestContext.addScriptFormat (
			"tdcheck_update ('%j')",
			nameValue);

	}

}
