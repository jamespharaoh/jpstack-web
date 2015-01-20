package wbs.platform.api.mvc;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.web.AbstractFile;
import wbs.framework.web.Action;
import wbs.framework.web.ActionRequestHandler;
import wbs.framework.web.RequestHandler;
import wbs.framework.web.Responder;
import wbs.framework.web.WebFile;

@Accessors (fluent = true)
@PrototypeComponent ("apiFile")
public
class ApiFile
	extends AbstractFile {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	@Inject
	WebApiManager webApiManager;

	// properties

	@Getter @Setter
	RequestHandler getHandler;

	@Getter @Setter
	RequestHandler postHandler;

	@Getter @Setter
	Map<String,Object> requestParams =
		new LinkedHashMap<String,Object> ();

	// utilities

	public
	ApiFile getAction (
			Action action) {

		return getHandler (
			actionToRequestHandler (action));

	}

	public
	ApiFile getActionName (
			String actionName) {

		return getHandler (
			actionNameToRequestHandler (actionName));

	}

	public
	ApiFile getResponder (
			final Provider<Responder> responderProvider) {

		RequestHandler requestHandler =
			new RequestHandler () {

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

		return getHandler (
			requestHandler);

	}

	public
	ApiFile getResponderName (
			final String beanName) {

		return getHandler (

			new RequestHandler () {

				@Override
				public
				void handle ()
					throws
						ServletException,
						IOException {

					Responder responder = (Responder)
						applicationContext.getBean (
							beanName,
							Responder.class);

					responder.execute ();

				}

			});

	}

	public
	ApiFile postAction (
			Action action) {

		return postHandler (
			new ActionRequestHandler (action));

	}

	public
	ApiFile postActionName (
			final String beanName) {

		return postAction (
			new Action () {

				@Override
				public
				Responder go ()
					throws ServletException {

					Action action =
						applicationContext.getBean (
							beanName,
							Action.class);

					return action.go ();

				}

			});

	}

	public
	WebFile postApiAction (
			WebApiAction webApiAction) {

		return postHandler (
			webApiManager.makeWebApiActionRequestHandler (
				webApiAction));

	}

}
