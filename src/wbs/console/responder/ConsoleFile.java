package wbs.console.responder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContextPrivLookup;
import wbs.console.lookup.BooleanLookup;
import wbs.console.module.ConsoleManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.web.AbstractFile;
import wbs.framework.web.Action;
import wbs.framework.web.ActionRequestHandler;
import wbs.framework.web.ForbiddenException;
import wbs.framework.web.RequestHandler;
import wbs.framework.web.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("consoleFile")
@DataClass ("console-file")
public
class ConsoleFile
	extends AbstractFile {

	// singleton dependencies

	@WeakSingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	ConsoleRequestContext consoleRequestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ActionRequestHandler> actionRequestHandlerProvider;

	@PrototypeDependency
	Provider <ConsoleContextPrivLookup> contextPrivLookupProvider;

	// properties

	@DataAttribute
	@Getter @Setter
	RequestHandler getHandler;

	@DataAttribute
	@Getter @Setter
	RequestHandler postHandler;

	@DataAttribute
	@Getter @Setter
	BooleanLookup privLookup;

	@DataChildren
	@Getter @Setter
	Map<String,Object> requestParams =
		new LinkedHashMap<String,Object> ();

	// utility methods

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

				Provider <Responder> responderProvider =
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
			actionRequestHandlerProvider.get ()
				.action (action));

	}

	public
	ConsoleFile getActionName (
			String name) {

		if (name == null)
			return this;

		return getHandler (
			actionRequestHandlerProvider.get ()
				.actionName (name));

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
			actionRequestHandlerProvider.get ()
				.action (action));

	}

	public
	ConsoleFile postActionName (
			String actionName) {

		if (actionName == null)
			return this;

		return postHandler (
			actionRequestHandlerProvider.get ()
				.actionName (actionName));

	}

	public
	ConsoleFile privKeys (
			List<String> privKeys) {

		return privLookup (
			contextPrivLookupProvider.get ()
				.privKeys (privKeys));

	}

	public
	ConsoleFile privName (
			String privName) {

		return privLookup (
			contextPrivLookupProvider.get ()
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

	// implementation

	@Override
	public
	void doGet ()
		throws
			ServletException,
			IOException {

		if (

			privLookup != null

			&& ! privLookup.lookup (
				consoleRequestContext.contextStuff ())

		) {

			throw new ForbiddenException (
				privLookup.describe ());

		}

		super.doGet ();

	}

	@Override
	public
	void doPost ()
		throws
			ServletException,
			IOException {

		if (

			privLookup != null

			&& ! privLookup.lookup (
				consoleRequestContext.contextStuff ())

		) {

			throw new ForbiddenException ();

		}

		super.doPost ();

	}

}
