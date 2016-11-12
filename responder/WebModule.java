package wbs.web.responder;

import java.util.Map;

import wbs.web.file.WebFile;
import wbs.web.pathhandler.PathHandler;

public
interface WebModule {

	Map <String, PathHandler> paths ();

	Map <String, WebFile> files ();

}
