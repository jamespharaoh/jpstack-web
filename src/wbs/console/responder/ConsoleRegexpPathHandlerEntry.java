package wbs.console.responder;

import java.util.regex.Matcher;

import javax.servlet.ServletException;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.web.context.RequestContext;
import wbs.web.file.WebFile;
import wbs.web.pathhandler.RegexpPathHandler;

public abstract
class ConsoleRegexpPathHandlerEntry
	extends RegexpPathHandler.Entry {

	// singleton dependencies

	@SingletonDependency
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
