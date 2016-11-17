package wbs.integrations.txtnation.api;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.web.file.WebFile;
import wbs.web.pathhandler.PathHandler;
import wbs.web.pathhandler.RegexpPathHandler;
import wbs.web.responder.WebModule;

@SingletonComponent ("txtNationApiServletModule")
public
class TxtNationApiServletModule
	implements WebModule {

	// singleton dependencies

	@SingletonDependency
	TxtNationRoutePathHandlerEntry txtNationRoutePathHandlerEntry;

	// state

	PathHandler pathHandler;

	// life cycle

	@NormalLifecycleSetup
	public
	void init () {

		pathHandler =
			new RegexpPathHandler (
				txtNationRoutePathHandlerEntry);

	}

	// implementation

	@Override
	public
	Map <String, PathHandler> paths () {

		return ImmutableMap.<String, PathHandler> builder ()

			.put (
				"/txtnation",
				pathHandler)

			.build ();

	}

	@Override
	public
	Map<String,WebFile> files () {

		return ImmutableMap.<String,WebFile>builder ()
			.build ();

	}

}
