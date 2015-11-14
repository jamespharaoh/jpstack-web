package wbs.integrations.digitalselect.api;

import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RegexpPathHandler;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;

@SingletonComponent ("digitalSelectApiServletModule")
public
class DigitalSelectApiServletModule
	implements ServletModule {

	@Inject
	DigitalSelectRoutePathHandlerEntry digitalSelectRoutePathHandlerEntry;

	@Override
	public
	Map<String,PathHandler> paths () {

		return ImmutableMap.<String,PathHandler>builder ()

			.put (
				"/digitalselect",
				new RegexpPathHandler (
					digitalSelectRoutePathHandlerEntry))

			.build ();

	}

	@Override
	public
	Map<String,WebFile> files () {

		return ImmutableMap.<String,WebFile>builder ()
			.build ();

	}

}
