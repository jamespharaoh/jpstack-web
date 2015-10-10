package wbs.console.html;

import static wbs.framework.utils.etc.Misc.equal;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import wbs.framework.utils.etc.Html;

public
class HtmlSelectOptionList {

	static
	class HtmlSelectOption {

		String value;
		String text;

		HtmlSelectOption (
				String newValue,
				String newText) {

			value = newValue;
			text = newText;

		}

	}

	List<HtmlSelectOption> htmlSelectOptions =
		new ArrayList<HtmlSelectOption> ();

	public
	HtmlSelectOptionList () {
	}

	public
	HtmlSelectOptionList add (
			String value,
			String text) {

		htmlSelectOptions.add (
			new HtmlSelectOption (
				value,
				text));

		return this;

	}

	public
	HtmlSelectOptionList add (
			String text) {

		htmlSelectOptions.add (
			new HtmlSelectOption (
				text,
				text));

		return this;

	}

	public
	void go (
			PrintWriter out,
			String selectedValue) {

		for (HtmlSelectOption htmlSelectOption
				: htmlSelectOptions) {

			out.print (
				"<option value=\"" + Html.encode (htmlSelectOption.value) + "\"");

			if (
				selectedValue != null
				&& equal (
					htmlSelectOption.value,
					selectedValue)
			) {

				out.print (
					" selected");

			}

			out.println (
				">" + Html.encode (htmlSelectOption.text) + "</option>");

		}

	}

}
