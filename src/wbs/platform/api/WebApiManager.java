package wbs.platform.api;

import java.io.IOException;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.web.RequestContext;
import wbs.framework.web.RequestHandler;
import wbs.framework.web.Responder;
import wbs.platform.exception.logic.ExceptionLogic;

@SingletonComponent ("webApiManager")
public
class WebApiManager {

	@Inject
	RequestContext requestContext;

	@Inject
	ExceptionLogic exceptionLogic;

	public
	class WebApiActionRequestHandler
		implements RequestHandler {

		final
		WebApiAction action;

		public
		WebApiActionRequestHandler (
				WebApiAction action) {

			this.action =
				action;

		}

		@Override
		public
		void handle ()
			throws IOException {

			try {

				Responder responder =
					action.go ();

				if (responder != null) {
					responder.execute ();
					return;
				}

			} catch (Exception exception) {

				exceptionLogic.logException (
					"webapi",
					requestContext.requestUri (),
					exception,
					null,
					false);

			}

			Responder responder =
				action.makeFallbackResponder ()
					.get ();

			responder.execute ();

		}

	}

	public
	RequestHandler makeWebApiActionRequestHandler (
			WebApiAction action) {

		return new WebApiActionRequestHandler (
			action);

	}

}
