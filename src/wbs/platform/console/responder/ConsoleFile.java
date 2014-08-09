package wbs.platform.console.responder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.AbstractFile;
import wbs.framework.web.Action;
import wbs.framework.web.ForbiddenException;
import wbs.framework.web.RequestHandler;
import wbs.framework.web.Responder;
import wbs.platform.console.context.ConsoleContextPrivLookup;
import wbs.platform.console.lookup.BooleanLookup;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.request.ConsoleRequestContext;

@Accessors (fluent = true)
@PrototypeComponent ("consoleFile")
public
class ConsoleFile
	extends AbstractFile {

	// dependencies

	@Inject
	ConsoleRequestContext consoleRequestContext;

	// indirect dependencies

	@Inject
	Provider<ConsoleManager> consoleManagerProvider;

	// prototype dependencies

	@Inject
	Provider<ConsoleContextPrivLookup> contextPrivLookup;

	// properties

	@Getter @Setter
	RequestHandler getHandler;

	@Getter @Setter
	RequestHandler postHandler;

	@Getter @Setter
	BooleanLookup privLookup;

	@Getter @Setter
	Map<String,Object> requestParams =
		new LinkedHashMap<String,Object> ();

	public
	ConsoleFile getHandlerName (
			String handlerName) {

		if (handlerName == null)
			return this;

		return getHandler (
			handlerNameToRequestHandler (
				handlerName));

	}

	public
	ConsoleFile getResponder (
			Provider<Responder> responder) {

		return getHandler (
			responderToRequestHandler (
				responder));

	}

	public
	ConsoleFile getResponderName (
			String responderName) {

		if (responderName == null)
			return this;

		return getHandler (
			responderToRequestHandler (
				responder (responderName)));

	}

	public
	Provider<Responder> responder (
			final String responderName) {

		return new Provider<Responder> () {

			@Override
			public
			Responder get () {

				ConsoleManager consoleManager =
					consoleManagerProvider.get ();

				Provider<Responder> responderProvider =
					consoleManager.responder (
						responderName,
						true);

				return responderProvider.get ();

			}

		};

	}

	public
	ConsoleFile getAction (
			Action action) {

		return getHandler (
			actionToRequestHandler (
				action));

	}

	public
	ConsoleFile getActionName (
			String name) {

		if (name == null)
			return this;

		return getHandler (
			actionNameToRequestHandler (
				name));

	}

	public
	ConsoleFile postHandlerName (
			String handlerName) {

		return postHandler (
			handlerNameToRequestHandler (
				handlerName));

	}

	public
	ConsoleFile postAction (
			Action action) {

		return postHandler (
			actionToRequestHandler (
				action));

	}

	public
	ConsoleFile postActionName (
			String actionName) {

		if (actionName == null)
			return this;

		return postHandler (
			actionNameToRequestHandler (
				actionName));

	}

	public
	ConsoleFile privKeys (
			List<String> privKeys) {

		return privLookup (
			contextPrivLookup.get ()
				.privKeys (privKeys));

	}

	public
	ConsoleFile privName (
			String privName) {

		return privLookup (
			contextPrivLookup.get ()
				.addPrivKey (privName));

	}

	public
	ConsoleFile requestParam (
			String key,
			Object value) {

		requestParams.put (
			key,
			value);

		return this;

	}

	@Override
	public
	void doGet ()
		throws
			ServletException,
			IOException {

		if (privLookup != null
				&& ! privLookup.lookup (
					consoleRequestContext.contextStuff ())) {

			throw new ForbiddenException ();

		}

		super.doGet ();

	}

	@Override
	public
	void doPost ()
		throws
			ServletException,
			IOException {

		if (privLookup != null
				&& ! privLookup.lookup (
					consoleRequestContext.contextStuff ()))
			throw new ForbiddenException ();

		super.doPost ();

	}

}
