package wbs.web.action;

import static wbs.utils.etc.NullUtils.isNull;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.handler.RequestHandler;
import wbs.web.responder.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("actionRequestHandler")
public
class ActionRequestHandler
	implements RequestHandler {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	Provider <Action> actionProvider;

	// utils

	public
	ActionRequestHandler actionName (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String actionName) {

		return actionProvider (
			componentManager.getComponentProviderRequired (
				parentTaskLogger,
				actionName,
				Action.class));

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

			Action action =
				actionProvider.get ();

			Responder responder =
				action.handle (
					parentTaskLogger);

			if (
				isNull (
					responder)
			) {
				throw new NullPointerException ();
			}

			responder.execute (
				parentTaskLogger);

		}

	}

}
