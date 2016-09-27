package wbs.platform.postgresql.console;

import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("postgresqlTablesPart")
public
class PostgresqlTablesPart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlBodyContent () {

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Table",
			"Size");

		htmlTableClose ();

		htmlParagraphWrite (
			"TODO");

	}

}
