package wbs.console.part;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("notFoundPart")
public
class NotFoundPart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (

			"<p class=\"error\">Page not found</p>\n",

			"<p>The requested page could not be found:</p>\n",

			"<p><code>%h</code></p>\n",
			requestContext.requestUri ());

	}

}
