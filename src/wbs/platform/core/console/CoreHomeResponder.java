package wbs.platform.core.console;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.responder.HtmlResponder;

@PrototypeComponent ("coreHomeResponder")
public
class CoreHomeResponder
	extends HtmlResponder {

	@Override
	protected
	void renderHtmlBodyContents () {

		printFormat (
			"<h1>Home</h1>\n");

		printFormat (
			"<p>Welcome to the SMS console.</p>\n");

	}

}
