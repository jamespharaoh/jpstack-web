package wbs.console.responder;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.exception.ExceptionUtils;

@Accessors (fluent = true)
@PrototypeComponent ("errorResponder")
public
class ErrorResponder
	extends HtmlResponder {

	// singleton dependencies

	@SingletonDependency
	ExceptionUtils exceptionLogic;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
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
			&& privChecker.canRecursive (
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
