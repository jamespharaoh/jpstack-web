package wbs.platform.console.responder;

import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.servlet.ServletException;

import wbs.framework.web.RegexpPathHandler;
import wbs.framework.web.RequestContext;
import wbs.framework.web.WebFile;
import wbs.platform.console.request.ConsoleRequestContext;

public abstract
class ConsoleRegexpPathHandlerEntry
	extends RegexpPathHandler.Entry {

	@Inject
	ConsoleRequestContext consoleRequestContext;

	public
	ConsoleRegexpPathHandlerEntry (
			String patternString) {

		super (
			patternString);

	}

	protected abstract
	WebFile handle (
			ConsoleRequestContext consoleRequestContext,
			Matcher matcher)
		throws ServletException;

	protected final
	WebFile handle (
			RequestContext requestContext,
			Matcher matcher)
		throws ServletException {

		return handle (
			consoleRequestContext,
			matcher);

	}

}
