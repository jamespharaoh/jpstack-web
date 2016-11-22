package wbs.console.html;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("htmlTableCheckWriter")
public
class HtmlTableCheckWriter {

	// dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// properties

	Optional <String> name =
		optionalAbsent ();

	Optional <String> label =
		optionalAbsent ();

	Optional <Boolean> value =
		optionalAbsent ();

	Optional <Long> columnSpan =
		optionalAbsent ();

	// property setters

	public
	HtmlTableCheckWriter name (
			@NonNull String name) {

		if (
			optionalIsPresent (
				this.name)
		) {
			throw new IllegalStateException ();
		}

		this.name =
			optionalOf (
				name);

		return this;

	}

	public
	HtmlTableCheckWriter label (
			@NonNull String label) {

		if (
			optionalIsPresent (
				this.label)
		) {
			throw new IllegalStateException ();
		}

		this.label =
			optionalOf (
				label);

		return this;

	}

	public
	HtmlTableCheckWriter value (
			@NonNull Boolean value) {

		if (
			optionalIsPresent (
				this.value)
		) {
			throw new IllegalStateException ();
		}

		this.value =
			optionalOf (
				value);

		return this;

	}

	public
	HtmlTableCheckWriter columnSpan (
			@NonNull Long columnSpan) {

		if (
			optionalIsPresent (
				this.columnSpan)
		) {
			throw new IllegalStateException ();
		}

		this.columnSpan =
			optionalOf (
				columnSpan);

		return this;

	}

	public
	void write (
			@NonNull FormatWriter formatWriter) {

		// open the td

		formatWriter.writeLineFormat (

			"<td",

			" id=\"%h_td\"",
			name.get (),

			" class=\"%h\"",
			value.get ()
				? "selected"
				: "unselected",

			" style=\"cursor: pointer;\"",

			" onclick=\"%h\"",
			stringFormat (
				"tdcheck_td ('%j');",
				name.get ()),

			" onmouseover=\"%h\"",
			stringFormat (
				"tdcheck_focus ('%j');",
				name.get ()),

			" onmouseout=\"%h\"",
			stringFormat (
				"tdcheck_update ('%j');",
				name.get ()),

			">");

		formatWriter.increaseIndent ();

		formatWriter.writeLineFormat (
			"<table class=\"layout\">");

		formatWriter.increaseIndent ();

		formatWriter.writeLineFormat (
			"<tr>");

		formatWriter.increaseIndent ();

		formatWriter.writeLineFormat (

			"<td><input",
			" type=\"checkbox\"",

			" id=\"%h\"",
			name.get (),

			" name=\"%h\"",
			name.get (),

			" onfocus=\"%h\"",
			stringFormat (
				"tdcheck_focus ('%j');",
				name.get ()),

			" onblur=\"%h\"",
			stringFormat (
				"tdcheck_update ('%j');",
				name.get ()),

			" onclick=\"%h\"",
			stringFormat (
				"tdcheck_checkbox ('%j', event);",
				name.get ()),

			"%s",
			value.get ()
				? " checked"
				: "",

			">",

			"</td>");

		formatWriter.writeLineFormat (
			"<td>&nbsp;</td>");

		formatWriter.writeLineFormat (
			"<td>%h</td>",
			label.get ());

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</tr>");

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</table>");

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</td>");

		// add this to the list of script bits still to do

		requestContext.addScriptFormat (
			"tdcheck_update ('%j');",
			optionalGetRequired (
				name));

	}

}
