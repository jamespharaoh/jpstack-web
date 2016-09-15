package wbs.apn.chat.infosite.api;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Provider;

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

@SingletonComponent ("chatInfoSiteApiModule")
public
class ChatInfoSiteApiModule
	implements ServletModule {

	// singleton dependencies

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
	void init () {

		infoSiteFile =
			apiFile.get ()
				.getActionName ("chatInfoSiteViewAction")
				.postActionName ("chatInfoSiteRespondAction");

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