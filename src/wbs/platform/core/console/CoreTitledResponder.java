package wbs.platform.core.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.OptionalUtils.optionalOrEmptyString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingOneWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteHtml;

import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.html.ScriptRef;
import wbs.console.part.PagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.console.responder.ConsoleHtmlResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleLogic;

@Accessors (fluent = true)
@PrototypeComponent ("coreTitledResponder")
public
class CoreTitledResponder
	extends ConsoleHtmlResponder {

	// singleton dependencies

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	ExceptionUtils exceptionLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// properties

	@Getter @Setter
	String title;

	@Getter @Setter
	PagePart pagePart;

	// state

	Throwable pagePartThrew;

	// details

	@Override
	protected
	Set <ScriptRef> scriptRefs () {
		return pagePart.scriptRefs ();
	}

	// implementation

	@Override
	protected
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			super.setup (
				taskLogger);

			pagePart.setup (
				taskLogger,
				emptyMap ());

		}

	}

	@Override
	protected
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"prepare");

		) {

			super.prepare (
				taskLogger);

			if (pagePart != null) {

				try {

					pagePart.prepare (
						taskLogger);

				} catch (RuntimeException exception) {

					// record the exception

					String path =
						stringFormat (
							"%s%s",
							requestContext.servletPath (),
							optionalOrEmptyString (
								requestContext.pathInfo ()));

					exceptionLogger.logThrowable (
						taskLogger,
						"console",
						path,
						exception,
						userConsoleLogic.userId (),
						GenericExceptionResolution.ignoreWithUserWarning);

					// and remember we had a problem

					pagePartThrew =
						exception;

					requestContext.addError (
						"Internal error");

				}

			}

		}

	}

	@Override
	protected
	void renderHtmlHeadContents (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"renderHtmlHeadContents");

		) {

			super.renderHtmlHeadContents (
				taskLogger);

			formatWriter.writeLineFormat (
				"<link",
				" rel=\"stylesheet\"",
				" href=\"%h\"",
				requestContext.resolveApplicationUrl (
					"/style/basic.css"),
				">");

			pagePart.renderHtmlHeadContent (
				taskLogger);

		}

	}

	protected
	void goTab () {
	}

	@Override
	protected
	void renderHtmlBodyContents (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"renderHtmlBodyContents");

		) {

			htmlHeadingOneWrite (
				title);

			requestContext.flushNotices (
				formatWriter);

			if (pagePartThrew != null) {

				htmlParagraphWriteFormat (
					"Unable to show page contents.");

				if (
					privChecker.canRecursive (
						taskLogger,
						GlobalId.root,
						"debug")
				) {

					htmlParagraphWriteHtml (
						stringFormat (
							"<pre>%h</pre>",
							exceptionLogic.throwableDump (
								taskLogger,
								pagePartThrew)));

				}

			} else if (pagePart != null) {

				pagePart.renderHtmlBodyContent (
					taskLogger);

			}

		}

	}

}
