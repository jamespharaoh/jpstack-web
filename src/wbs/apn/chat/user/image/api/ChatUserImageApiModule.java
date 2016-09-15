package wbs.apn.chat.user.image.api;

import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import wbs.api.mvc.ApiFile;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RegexpPathHandler;
import wbs.framework.web.RequestContext;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;

@SingletonComponent ("chatUserImageApiModule")
public
class ChatUserImageApiModule
	implements ServletModule {

	// singleton dependencies

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ApiFile> apiFileProvider;

	// state

	WebFile imageUploadFile;
	RegexpPathHandler.Entry imageUploadEntry;

	// implementation

	@NormalLifecycleSetup
	public
	void init () {

		imageUploadFile =
			apiFileProvider.get ()

			.getActionName (
				"chatUserImageUploadViewAction")

			.postActionName (
				"chatUserImageUploadPostAction");

		imageUploadEntry =
			new RegexpPathHandler.Entry (
				"/([a-z]+)") {

			@Override
			protected
			WebFile handle (
					Matcher matcher) {

				requestContext.request (
					"chatUserImageUploadToken",
					matcher.group (1));

				return imageUploadFile;

			}

		};

	}

	@Override
	public
	Map<String,PathHandler> paths () {

		return ImmutableMap.<String,PathHandler>builder ()

			.put (
				"/chat/imageUpload",
				new RegexpPathHandler (
					imageUploadEntry))

			.build ();

	}

	@Override
	public
	Map<String,WebFile> files () {

		return ImmutableMap.<String,WebFile>builder ()

			.build ();

	}

}
