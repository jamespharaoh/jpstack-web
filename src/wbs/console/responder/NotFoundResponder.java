package wbs.console.responder;

import javax.inject.Inject;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("notFoundResponder")
public
class NotFoundResponder
	extends HtmlResponder {

	@Inject
	ConsoleRequestContext requestContext;

	@Override
	public
	void renderHtmlBodyContents () {

		printFormat (
			"<h1>Page not found</h1>\n");

		printFormat (
			"<p class=\"error\">Page not found</p>\n");

		printFormat (
			"<p>The requested page could not be found:</p>\n");

		printFormat (
			"<p><code>%h</code></p>\n",
			requestContext.requestUri ());

	}

}
