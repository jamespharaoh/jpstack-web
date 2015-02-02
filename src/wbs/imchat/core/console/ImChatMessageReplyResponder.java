package wbs.imchat.core.console;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.responder.HtmlResponder;

@PrototypeComponent ("imChatMessageReplyResponder")
public
class ImChatMessageReplyResponder
	extends HtmlResponder {

	@Override
	protected
	void goBodyStuff () {

		printFormat (
			"Hello world!\n");

	}

}
