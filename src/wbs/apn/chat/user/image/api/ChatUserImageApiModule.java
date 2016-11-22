package wbs.apn.chat.user.image.api;

import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.api.mvc.ApiFile;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.web.context.RequestContext;
import wbs.web.file.WebFile;
import wbs.web.pathhandler.PathHandler;
import wbs.web.pathhandler.RegexpPathHandler;
import wbs.web.responder.WebModule;

@SingletonComponent ("chatUserImageApiModule")
public
class ChatUserImageApiModule
	implements WebModule {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

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
	void init (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"init");

		imageUploadFile =
			apiFileProvider.get ()

			.getActionName (
				taskLogger,
				"chatUserImageUploadViewAction")

			.postActionName (
				taskLogger,
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
