package wbs.framework.web;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;

@Accessors (fluent = true)
@PrototypeComponent ("actionRequestHandler")
public
class ActionRequestHandler
	implements RequestHandler {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	// properties

	@Getter @Setter
	Provider<Action> actionProvider;

	// utils
	
	public
	ActionRequestHandler action (
			final Action action) {

		actionProvider =
			new Provider<Action> () {

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
			String actionName) {

		return actionProvider (
			applicationContext.getBeanProvider (
				actionName,
				Action.class));

	}

	// implementation

	@Override
	public
	void handle ()
		throws
			ServletException,
			IOException {

		Action action =
			actionProvider.get ();

		Responder responder =
			action.handle ();

		if (responder == null)
			throw new NullPointerException ();

		responder.execute ();

	}

}
