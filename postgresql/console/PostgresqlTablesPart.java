package wbs.platform.postgresql.console;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("postgresqlTablesPart")
public
class PostgresqlTablesPart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Table</th>\n",
			"<th>Size</th>\n",
			"</tr>");

		printFormat (
			"</table>\n");

		printFormat (
			"<p>TODO</p>\n");

	}

}
