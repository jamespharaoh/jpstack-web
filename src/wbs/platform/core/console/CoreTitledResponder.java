package wbs.platform.core.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.html.ScriptRef;
import wbs.console.part.PagePart;
import wbs.console.priv.PrivChecker;
import wbs.console.responder.HtmlResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionLogic;
import wbs.framework.record.GlobalId;

import com.google.common.base.Optional;

@Accessors (fluent = true)
@PrototypeComponent ("coreTitledResponder")
public
class CoreTitledResponder
	extends HtmlResponder {

	// dependencies

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	PrivChecker privChecker;

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
	Set<ScriptRef> scriptRefs () {
		return pagePart.scriptRefs ();
	}

	// implementation

	@Override
	protected
	void setup ()
		throws IOException {

		super.setup ();

		pagePart.setup (
			Collections.<String,Object>emptyMap ());

	}

	@Override
	protected
	void prepare () {

		super.prepare ();

		if (pagePart != null) {

			try {

				pagePart.prepare ();

			} catch (RuntimeException exception) {

				// record the exception

				String path =
					stringFormat (
						"%s%s",
						requestContext.servletPath (),
						requestContext.pathInfo () != null
							? requestContext.pathInfo ()
							: "");

				exceptionLogger.logThrowable (
					"console",
					path,
					exception,
					Optional.fromNullable (
						requestContext.userId ()),
					false);

				// and remember we had a problem

				pagePartThrew = exception;

				requestContext.addError ("Internal error");

			}

		}

	}

	@Override
	protected
	void renderHtmlHeadContents () {

		super.renderHtmlHeadContents ();

		printFormat (
			"<link",
			" rel=\"stylesheet\"",
			" href=\"%h\"",
			requestContext.resolveApplicationUrl (
				"/style/basic.css"),
			">\n");

		pagePart.renderHtmlHeadContent ();

	}

	protected
	void goTab () {
	}

	@Override
	protected
	void renderHtmlBodyContents () {

		printFormat (
			"<h1>%h</h1>\n",
			title);

		requestContext.flushNotices (
			printWriter);

		if (pagePartThrew != null) {

			printFormat (
				"<p>Unable to show page contents.</p>\n");

			if (
				privChecker.can (
					GlobalId.root,
					"debug")
			) {

				printFormat (
					"<p><pre>%h</pre></p>\n",
					exceptionLogic.throwableDump (
						pagePartThrew));

			}

		} else if (pagePart != null) {

			pagePart.renderHtmlBodyContent ();

		}

	}

}
