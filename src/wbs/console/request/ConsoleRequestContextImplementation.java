package wbs.console.request;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.priv.UserPrivDataLoader;

import wbs.framework.component.annotations.ProxiedRequestComponent;
import wbs.framework.component.annotations.SingletonDependency;

import wbs.web.context.RequestContext;

@ProxiedRequestComponent (
	value = "consoleRequestContext",
	proxyInterface = ConsoleRequestContext.class)
public
class ConsoleRequestContextImplementation
	implements ConsoleRequestContext {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivDataLoader privChecker;

	@SingletonDependency
	RequestContext requestContext;

	// console request context core implementation

	@Override
	public
	RequestContext requestContext () {
		return requestContext;
	}

	// request context core implementation

	@Override
	public
	ServletContext context () {
		return requestContext.context ();
	}

	@Override
	public
	HttpServletRequest request () {
		return requestContext.request ();
	}

	@Override
	public
	HttpServletResponse response () {
		return requestContext.response ();
	}

}
