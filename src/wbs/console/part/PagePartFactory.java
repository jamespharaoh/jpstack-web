package wbs.console.part;

import wbs.framework.database.Transaction;

public
interface PagePartFactory {

	PagePart buildPagePart (
			Transaction parentTransaction);

}
