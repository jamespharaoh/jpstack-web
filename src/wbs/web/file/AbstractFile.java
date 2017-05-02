package wbs.web.file;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import java.util.Map;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.action.ActionRequestHandler;
import wbs.web.context.RequestContext;
import wbs.web.exceptions.HttpMethodNotAllowedException;
import wbs.web.handler.RequestHandler;
import wbs.web.responder.Responder;

/**
 * WebFile which delegates to "RequestHandlers".
 *
 * Many utility functions are provided which create RequestHandlers
 * automatically to delegate to Responders, WebActions and more.
 */
public abstract
class AbstractFile
	implements WebFile {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ActionRequestHandler> actionRequestHandlerProvider;

	// extension points

	public abstract
	RequestHandler getHandler ();

	public abstract
	RequestHandler postHandler ();

	public abstract
	Map <String, Object> requestParams ();

	public
	void setRequestParams () {

		for (
			Map.Entry <String, Object> ent
				: requestParams ().entrySet ()
		) {

			requestContext.request (
				ent.getKey (),
				ent.getValue ());

		}

	}

	@Override
	public
	void doGet (
			@NonNull TaskLogger parentTaskLogger) {

		if (getHandler () != null) {

			setRequestParams ();

			getHandler ().handle (
				parentTaskLogger);

		} else {

			throw new HttpMethodNotAllowedException (
				optionalAbsent (),
				emptyList ());

		}

	}

	@Override
	public
	void doPost (
			@NonNull TaskLogger parentTaskLogger) {

		if (postHandler () != null) {

			setRequestParams ();

			postHandler ().handle (
				parentTaskLogger);

		} else {

			throw new HttpMethodNotAllowedException (
				optionalAbsent (),
				emptyList ());

		}

	}

	@Override
	public
	void doOptions (
			@NonNull TaskLogger parentTaskLogger) {

	}

	public
	RequestHandler handlerNameToRequestHandler (
			@NonNull String handlerName) {

		return new RequestHandler () {

			@Override
			public
			void handle (
					@NonNull TaskLogger parentTaskLogger) {

				try (

					OwnedTaskLogger taskLogger =
						logContext.nestTaskLogger (
							parentTaskLogger,
							"handlerNameToRequestHandler.handle");

				) {

					RequestHandler handler =
						componentManager.getComponentRequired (
							parentTaskLogger,
							handlerName,
							RequestHandler.class);

					handler.handle (
						parentTaskLogger);

				}

			}

		};

	}

	public
	RequestHandler responderToRequestHandler (
			final Provider<Responder> responderProvider) {

		return new RequestHandler () {

			@Override
			public
			void handle (
					@NonNull TaskLogger parentTaskLogger) {

				try (

					OwnedTaskLogger taskLogger =
						logContext.nestTaskLogger (
							parentTaskLogger,
							"responderToRequestHandler.handle");

				) {

					Responder responder =
						responderProvider.get ();

					responder.execute (
						parentTaskLogger);

				}

			}

		};

	}

	public
	RequestHandler responderToRequestHandler (
			final String responderName) {

		return new RequestHandler () {

			@Override
			public
			void handle (
					@NonNull TaskLogger parentTaskLogger) {

				try (

					OwnedTaskLogger taskLogger =
						logContext.nestTaskLogger (
							parentTaskLogger,
							"responderToRequestHandler.handle");

				) {

					Responder responder =
						componentManager.getComponentRequired (
							parentTaskLogger,
							responderName,
							Responder.class);

					responder.execute (
						parentTaskLogger);

				}

			}

		};

	}

}
