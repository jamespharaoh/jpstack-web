package wbs.framework.web;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;

import wbs.framework.application.context.ApplicationContext;

/**
 * WebFile which delegates to "RequestHandlers".
 *
 * Many utility functions are provided which create RequestHandlers
 * automatically to delegate to Responders, WebActions and more.
 */
public abstract
class AbstractFile
	implements WebFile {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	@Inject
	RequestContext requestContext;

	// prototype dependencies

	@Inject
	Provider<ActionRequestHandler> actionRequestHandlerProvider;

	// extension points

	public abstract
	RequestHandler getHandler ();

	public abstract
	RequestHandler postHandler ();

	public abstract
	Map<String,Object> requestParams ();

	public
	void setRequestParams () {

		for (Map.Entry<String,Object> ent
				: requestParams ().entrySet ()) {

			requestContext.request (
				ent.getKey (),
				ent.getValue ());

		}

	}

	@Override
	public
	void doGet ()
		throws
			ServletException,
			IOException {

		if (getHandler () != null) {

			setRequestParams ();

			getHandler ().handle ();

		} else {

			throw new MethodNotAllowedException ();

		}

	}

	@Override
	public
	void doPost ()
		throws
			ServletException,
			IOException {

		if (postHandler () != null) {

			setRequestParams ();

			postHandler ().handle ();

		} else {

			throw new MethodNotAllowedException ();

		}

	}

	@Override
	public
	void doOptions ()
		throws
			ServletException,
			IOException {

	}

	public
	RequestHandler handlerNameToRequestHandler (
			final String handlerName) {

		return new RequestHandler () {

			@Override
			public
			void handle ()
				throws
					ServletException,
					IOException {

				RequestHandler handler =
					applicationContext.getBean (
						handlerName,
						RequestHandler.class);

				handler.handle ();

			}

		};

	}

	public
	RequestHandler responderToRequestHandler (
			final Provider<Responder> responderProvider) {

		return new RequestHandler () {

			@Override
			public
			void handle ()
				throws
					ServletException,
					IOException {

				Responder responder =
					responderProvider.get ();

				responder.execute ();

			}

		};

	}

	public
	RequestHandler responderToRequestHandler (
			final String responderName) {

		return new RequestHandler () {

			@Override
			public
			void handle ()
				throws
					ServletException,
					IOException {

				Responder responder =
					applicationContext.getBean (
						responderName,
						Responder.class);

				responder.execute ();

			}

		};

	}

}
