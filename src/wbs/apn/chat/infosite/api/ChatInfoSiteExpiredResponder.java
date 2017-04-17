package wbs.apn.chat.infosite.api;

import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.web.context.RequestContext;
import wbs.web.responder.PrintResponder;

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
	void goHeaders (
			@NonNull TaskLogger parentTaskLogger) {

		requestContext.setHeader (
			"Content-Type",
			"text/html");

	}

	@Override
	protected
	void goContent (
		@NonNull TaskLogger parentTaskLogger) {

		htmlParagraphWrite (
			"This message is no longer valid.");

	}

}