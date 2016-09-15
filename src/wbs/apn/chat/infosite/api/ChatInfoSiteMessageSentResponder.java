package wbs.apn.chat.infosite.api;

import java.io.IOException;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.web.PrintResponder;
import wbs.framework.web.RequestContext;

@PrototypeComponent ("chatInfoSiteMessageSentResponder")
public
class ChatInfoSiteMessageSentResponder
	extends PrintResponder {

	// singleton dependencies

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
			"<p>Your message has been sent.</p>\n");

	}

}
