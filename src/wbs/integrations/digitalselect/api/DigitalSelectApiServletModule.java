package wbs.integrations.digitalselect.api;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

import wbs.web.file.WebFile;
import wbs.web.pathhandler.PathHandler;
import wbs.web.pathhandler.RegexpPathHandler;
import wbs.web.responder.WebModule;

@SingletonComponent ("digitalSelectApiServletModule")
public
class DigitalSelectApiServletModule
	implements WebModule {

	// singleton dependencies

	@SingletonDependency
	DigitalSelectRoutePathHandlerEntry digitalSelectRoutePathHandlerEntry;

	// implementation

	@Override
	public
	Map <String, PathHandler> paths () {

		return ImmutableMap.<String,PathHandler>builder ()

			.put (
				"/digitalselect",
				new RegexpPathHandler (
					digitalSelectRoutePathHandlerEntry))

			.build ();

	}

	@Override
	public
	Map <String, WebFile> files () {

		return ImmutableMap.<String,WebFile>builder ()
			.build ();

	}

}
