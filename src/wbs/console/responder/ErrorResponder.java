package wbs.console.responder;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.priv.PrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.exception.ExceptionLogic;
import wbs.framework.record.GlobalId;

@Accessors (fluent = true)
@PrototypeComponent ("errorResponder")
public
class ErrorResponder
	extends HtmlResponder {

	// dependencies

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	PrivChecker privChecker;

	@Inject
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	Throwable exception;

	@Getter @Setter
	String title = defaultTitle;

	@Getter @Setter
	String message = defaultMessage;

	// implementation

	@Override
	public
	void renderHtmlBodyContents () {

		printFormat (
			"<h1>%h</h1>\n",
			title);

		requestContext.flushNotices ();

		printFormat (
			"<p class=\"error\">%h</p>\n",
			title);

		printFormat (
			"<p>%h</p>\n",
			message);

		if (
			exception != null
			&& privChecker.can (
				GlobalId.root,
				"debug")
		) {

			printFormat (
				"<p><pre>%h</pre></p>\n",
				exceptionLogic.throwableDump (
					exception));

		}

	}

	// data

	public final static
	String defaultTitle =
		"Internal error.";

	public final static
	String defaultMessage =
		"This page cannot be displayed, due to an internal error.";

}
