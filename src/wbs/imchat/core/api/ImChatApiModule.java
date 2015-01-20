package wbs.imchat.core.api;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.web.PathHandler;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;
import wbs.platform.api.mvc.ApiFile;

import com.google.common.collect.ImmutableMap;

@SingletonComponent ("imChatApiModule")
public
class ImChatApiModule
	implements ServletModule {

	// dependencies

	@Inject
	Provider<ApiFile> apiFile;

	@Inject
	Database database;

	// implementation

	@Override
	public
	Map<String,PathHandler> paths () {

		return new HashMap<String,PathHandler> ();

	}

	@Override
	public
	Map<String,WebFile> files () {

		return ImmutableMap.<String,WebFile>builder ()

			.put (
				"/imchat/test",
				apiFile.get ()
					.postActionName ("imChatTestAction"))

			.build ();

	}

}
