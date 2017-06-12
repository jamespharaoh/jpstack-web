package wbs.web.mvc;

import static wbs.utils.string.StringUtils.keyEqualsDecimalInteger;

import javax.inject.Provider;

import lombok.NonNull;

import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.responder.WebResponder;

@PrototypeComponent ("webActionRequestHandler")
public
class WebActionRequestHandler
	implements WebRequestHandler {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	WebExceptionHandler webExceptionHandler;

	// properties

	Provider <WebAction> actionProvider;

	// details

	public final static
	long maxAttempts = 5;

	public final static
	Duration backoffDuration =
		Duration.millis (10l);

	// property setters

	public
	WebActionRequestHandler actionProvider (
			@NonNull Provider <WebAction> actionProvider) {

		this.actionProvider =
			actionProvider;

		return this;

	}

	public
	WebActionRequestHandler actionName (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String actionName) {

		return actionProvider (
			componentManager.getComponentProviderRequired (
				parentTaskLogger,
				actionName,
				WebAction.class));

	}

	// implementation

	@Override
	public
	void handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handle");

		) {

			for (
				long attempt = 0l;
				attempt < maxAttempts;
				attempt ++
			) {

				if (attempt < maxAttempts - 1) {

					try {

						handleOnce (
							taskLogger,
							attempt);

						return;

					} catch (Exception exception) {

						webExceptionHandler.handleExceptionRetry (
							taskLogger,
							attempt,
							exception);

					}

				} else {

					try {

						handleOnce (
							taskLogger,
							attempt);

					} catch (Exception exception) {

						webExceptionHandler.handleExceptionFinal (
							taskLogger,
							attempt,
							exception);

					}

				}

			}

		}

	}

	// private implementation

	private
	void handleOnce (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long attempt) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handleOnce",
					keyEqualsDecimalInteger (
						"attempt",
						attempt));

		) {

			WebAction action =
				actionProvider.get ();

			WebResponder responder =
				action.handle (
					taskLogger);

			responder.execute (
				taskLogger);

		}

	}

}
