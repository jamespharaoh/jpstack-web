package wbs.console.part;

import wbs.framework.logging.TaskLogger;

public
interface PagePartFactory {

	PagePart buildPagePart (
			TaskLogger taskLogger);

}
