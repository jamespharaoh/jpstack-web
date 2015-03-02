package wbs.clients.apn.chat.infosite.api;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RegexpPathHandler;
import wbs.framework.web.RequestContext;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;
import wbs.platform.api.mvc.ApiFile;

@SingletonComponent ("chatInfoSiteApiModule")
public
class ChatInfoSiteApiModule
	implements ServletModule {

	// dependencies

	@Inject
	RequestContext requestContext;

	@Inject
	Provider<ApiFile> apiFile;

	// state

	WebFile infoSiteFile;
	WebFile infoSiteImageFile;

	RegexpPathHandler.Entry infoSiteEntry;

	// implementation

	@PostConstruct
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
					Integer.parseInt (matcher.group (1)));

				requestContext.request (
					"chatInfoSiteToken",
					matcher.group (2));

				if (matcher.group (3) == null)
					return infoSiteFile;

				requestContext.request (
					"chatInfoSiteIndex",
					Integer.parseInt (matcher.group (3)));

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