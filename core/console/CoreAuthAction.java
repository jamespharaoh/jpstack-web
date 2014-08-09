package wbs.platform.core.console;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.lookup.BooleanLookup;
import wbs.platform.console.request.ConsoleRequestContext;

@Accessors (fluent = true)
@PrototypeComponent ("coreAuthAction")
public
class CoreAuthAction
	extends ConsoleAction {

	@Inject
	ConsoleRequestContext requestContext;

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
