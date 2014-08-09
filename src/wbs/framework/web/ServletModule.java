package wbs.framework.web;

import java.util.Map;

public
interface ServletModule {

	Map<String,PathHandler> paths ();

	Map<String,WebFile> files ();

}
