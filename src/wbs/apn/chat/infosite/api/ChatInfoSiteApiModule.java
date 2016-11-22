package wbs.apn.chat.infosite.api;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Provider;

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

@SingletonComponent ("chatInfoSiteApiModule")
public
class ChatInfoSiteApiModule
	implements WebModule {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ApiFile> apiFile;

	// state

	WebFile infoSiteFile;
	WebFile infoSiteImageFile;

	RegexpPathHandler.Entry infoSiteEntry;

	// implementation

	@NormalLifecycleSetup
	public
	void init (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"init");

		infoSiteFile =
			apiFile.get ()

			.getActionName (
				taskLogger,
				"chatInfoSiteViewAction")

			.postActionName (
				taskLogger,
				"chatInfoSiteRespondAction");

		infoSiteImageFile =
			apiFile.get ()
				.getResponderName ("chatInfoSiteImageResponder");

		infoSiteEntry =
			new RegexpPathHandler.Entry (
				"/([0-9]+)" +
				"/([a-z]+)" +
				"(?:" +
					"/([0-9]+)" +
					"/(full|normal)" +
				")?"
			) {

			@Override
			protected
			WebFile handle (
					Matcher matcher) {

				requestContext.request (
					"chatInfoSiteId",
					parseIntegerRequired (
						matcher.group (1)));

				requestContext.request (
					"chatInfoSiteToken",
					matcher.group (2));

				if (matcher.group (3) == null)
					return infoSiteFile;

				requestContext.request (
					"chatInfoSiteIndex",
					parseIntegerRequired (
						matcher.group (3)));

				requestContext.request (
					"chatInfoSiteMode",
					matcher.group (4));

				return infoSiteImageFile;

			}

		};

	}

	@Override
	public
	Map<String,PathHandler> paths () {

		Map<String,PathHandler> ret =
			new HashMap<String,PathHandler> ();

		ret.put (
			"/chat/infoSite",
			new RegexpPathHandler (infoSiteEntry));

		return ret;

	}

	@Override
	public
	Map<String,WebFile> files () {
		return null;
	}

}