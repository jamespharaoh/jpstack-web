package wbs.apn.chat.graphs.console;

import static wbs.framework.utils.etc.Misc.stringFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.platform.console.html.ObsoleteDateLinks;
import wbs.platform.console.html.ObsoleteMonthField;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.console.request.EmptyFormData;

@Accessors (fluent = true)
public
abstract class AbstractMonthlyGraphPart
	extends AbstractPagePart {

	@Getter @Setter
	String myLocalPart;

	@Getter @Setter
	String imageLocalPart;

	ObsoleteMonthField monthField;

	@Override
	public
	void prepare () {

		monthField = ObsoleteMonthField.parse (
			requestContext.parameter ("month"));
	}

	@Override
	public
	void goBodyStuff ()  {

		printFormat (
			"<form",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				myLocalPart),
			" method=\"get\"",
			">\n");

		printFormat (
			"<p>Month<br>\n",

			"<input",
			" type=\"text\"",
			" name=\"month\"",
			" value=\"%h\"",
			monthField.text,
			">",

			"<input",
			" type=\"submit\"",
			" value=\"ok\"",
			"></p>");

		printFormat (
			"</form>\n");

		if (monthField.date != null) {

			ObsoleteDateLinks.monthlyBrowser (
				out,
				requestContext.resolveLocalUrl (
					myLocalPart),
				EmptyFormData.instance,
				monthField.date);

			printFormat (
				"<p>");

			printFormat (
				"<img",
				" style=\"graph\"",
				" src=\"%h\"",
				requestContext.resolveLocalUrl (
					stringFormat (
						"%s",
						imageLocalPart,
						"?month=%u",
						monthField.text)),
				">");

			printFormat (
				"</p>\n");

		}

	}

}
