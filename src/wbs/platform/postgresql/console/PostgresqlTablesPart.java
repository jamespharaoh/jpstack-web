package wbs.platform.postgresql.console;

import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("postgresqlTablesPart")
public
class PostgresqlTablesPart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Table",
			"Size");

		htmlTableClose ();

		htmlParagraphWrite (
			"TODO");

	}

}
