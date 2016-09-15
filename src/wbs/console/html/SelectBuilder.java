package wbs.console.html;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("selectBuilder")
public
class SelectBuilder {

	// properties

	@Getter @Setter
	String id;

	@Getter @Setter
	String htmlClass;

	@Getter @Setter
	String selectName;

	@Getter @Setter
	String selectedValue;

	@Getter @Setter
	Map <String, String> options =
		new LinkedHashMap<> ();

	// implementation

	public
	String build () {

		StringBuilder stringBuilder =
			new StringBuilder ();

		stringBuilder.append (
			stringFormat (
				"<select"));

		if (id != null) {

			stringBuilder.append (
				stringFormat (
					" id=\"%h\"", id));

		}

		if (htmlClass != null) {

			stringBuilder.append (
				stringFormat (
					" class=\"%h\"",
					htmlClass));

		}

		if (selectName != null) {

			stringBuilder.append (
				stringFormat (
					" name=\"%h\"",
					selectName));

		}

		stringBuilder.append (
			stringFormat (
				">\n"));

		for (
			Map.Entry <String, String> optionEntry
				: options.entrySet ()
		) {

			String optionValue =
				optionEntry.getKey ();

			String optionText =
				optionEntry.getValue ();

			stringBuilder.append (
				stringFormat (
					"<option",

					" value=\"%h\"",
					optionValue,

					ifThenElse (
						stringEqualSafe (
							optionValue,
							selectedValue),
						() -> " selected",
						() -> ""),

					">%h</option>",
					optionText));

		}

		stringBuilder.append (
			stringFormat (
				"</select>\n"));

		return stringBuilder.toString ();

	}

}
