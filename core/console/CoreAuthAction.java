package wbs.platform.core.console;

import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.lookup.BooleanLookup;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.web.Responder;

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
	Provider<Responder> normalResponder;

	@Getter @Setter
	Provider<Responder> deniedResponder;

	@Override
	protected
	Responder backupResponder () {
		return null;
	}

	@Override
	public
	Responder goReal () {

		if (! lookup.lookup (
				requestContext.contextStuff ())) {

			requestContext.addError (
				"Access denied");

			return deniedResponder.get ();

		}

		return normalResponder.get ();

	}

}
