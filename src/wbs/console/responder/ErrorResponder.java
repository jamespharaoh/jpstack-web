package wbs.console.responder;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingOneWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWriteHtml;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
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

		htmlHeadingOneWrite (
			title);

		requestContext.flushNotices ();

		htmlParagraphWrite (
			title,
			htmlClassAttribute (
				"error"));

		htmlParagraphWrite (
			message);

		if (
			exception != null
			&& privChecker.canRecursive (
				GlobalId.root,
				"debug")
		) {

			htmlParagraphWriteHtml (
				stringFormat (
					"<pre>%h</pre>",
					exceptionLogic.throwableDump (
						exception)));

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
