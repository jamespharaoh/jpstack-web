package wbs.web.responder;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import wbs.web.file.WebFile;
import wbs.web.pathhandler.PathHandler;

public
interface WebModule {

	default
	Map <String, PathHandler> paths () {
		return ImmutableMap.of ();
	}

	default
	Map <String, WebFile> files () {
		return ImmutableMap.of ();
	}

}
