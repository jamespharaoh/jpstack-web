package wbs.web.action;

import static wbs.utils.etc.Misc.isNull;

import java.io.IOException;

import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
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

	// properties

	@Getter @Setter
	Provider <Action> actionProvider;

	// utils

	public
	ActionRequestHandler action (
			final Action action) {

		actionProvider =
			new Provider <Action> () {

			@Override
			public
			Action get () {
				return action;
			}

		};

		return this;

	}

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
			@NonNull TaskLogger parentTaskLogger)
		throws
			ServletException,
			IOException {

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
