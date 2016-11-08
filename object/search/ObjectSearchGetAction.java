package wbs.platform.object.search;

import static wbs.utils.etc.Misc.isNull;

import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;
import wbs.framework.web.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("objectSearchGetAction")
public
class ObjectSearchGetAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	String searchResponderName;

	@Getter @Setter
	String searchResultsResponderName;

	@Getter @Setter
	String sessionKey;

	// details

	@Override
	protected
	Responder backupResponder () {

		return responder (
			searchResponderName);

	}

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		Object searchObject =
			requestContext.session (
				sessionKey + "Fields");

		List<?> objectIds =
			(List<?>)
			requestContext.session (
				sessionKey + "Results");

		if (

			isNull (
				searchObject)

			|| isNull (
				objectIds)

		) {

			requestContext.session (
				sessionKey + "Results",
				null);

			return responder (
				searchResponderName);

		} else {

			return responder (
				searchResultsResponderName);

		}

	}

}
