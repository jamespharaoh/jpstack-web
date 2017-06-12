package wbs.web.file;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import java.util.Map;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.web.context.RequestContext;
import wbs.web.exceptions.HttpMethodNotAllowedException;
import wbs.web.mvc.WebActionRequestHandler;
import wbs.web.mvc.WebRequestHandler;
import wbs.web.mvc.WebResponderRequestHandler;

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

	@StrongPrototypeDependency
	Provider <WebActionRequestHandler> actionRequestHandlerProvider;

	@StrongPrototypeDependency
	Provider <WebResponderRequestHandler> responderRequestHandlerProvider;

	// extension points

	public abstract
	Provider <? extends WebRequestHandler> getHandlerProvider ();

	public abstract
	Provider <? extends WebRequestHandler> postHandlerProvider ();

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

		if (getHandlerProvider () != null) {

			setRequestParams ();

			getHandlerProvider ().get ().handle (
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

		if (postHandlerProvider () != null) {

			setRequestParams ();

			postHandlerProvider ().get ().handle (
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

}
