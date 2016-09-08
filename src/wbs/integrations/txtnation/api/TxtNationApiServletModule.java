package wbs.integrations.txtnation.api;

import java.util.Map;

import javax.annotation.PostConstruct;

import com.google.common.collect.ImmutableMap;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RegexpPathHandler;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;

@SingletonComponent ("txtNationApiServletModule")
public
class TxtNationApiServletModule
	implements ServletModule {

	// singleton dependencies

	@SingletonDependency
	TxtNationRoutePathHandlerEntry txtNationRoutePathHandlerEntry;

	// state

	PathHandler pathHandler;

	// life cycle

	@PostConstruct
	public
	void init () {

		pathHandler =
			new RegexpPathHandler (
				txtNationRoutePathHandlerEntry);

	}

	@Override
	public
	Map<String,PathHandler> paths () {

		return ImmutableMap.<String,PathHandler>builder ()

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
