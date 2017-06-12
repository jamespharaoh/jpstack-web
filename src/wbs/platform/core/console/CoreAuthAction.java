package wbs.platform.core.console;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.lookup.BooleanLookup;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.web.responder.WebResponder;

@Accessors (fluent = true)
@PrototypeComponent ("coreAuthAction")
public
class CoreAuthAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	BooleanLookup lookup;

	@Getter @Setter
	Provider <WebResponder> normalResponderProvider;

	@Getter @Setter
	Provider <WebResponder> deniedResponderProvider;

	@Override
	public
	WebResponder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		if (
			! lookup.lookup (
				requestContext.consoleContextStuffRequired ())
		) {

			requestContext.addError (
				"Access denied");

			return deniedResponderProvider.get ();

		}

		return normalResponderProvider.get ();

	}

}
