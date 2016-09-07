package wbs.clients.apn.chat.infosite.api;

import java.io.IOException;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.web.PrintResponder;
import wbs.framework.web.RequestContext;

@PrototypeComponent ("chatInfoSiteExpiredResponder")
public
class ChatInfoSiteExpiredResponder
	extends PrintResponder {

	// dependencies

	@SingletonDependency
	RequestContext requestContext;

	// implementation

	@Override
	protected
	void goHeaders ()
		throws IOException {

		requestContext.setHeader (
			"Content-Type",
			"text/html");

	}

	@Override
	protected
	void goContent ()
		throws IOException {

		printFormat (
			"<p>This message is no longer valid.</p>\n");

	}

}