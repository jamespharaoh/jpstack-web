package wbs.platform.object.search;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("objectSearchGetAction")
public
class ObjectSearchGetAction
	extends ConsoleAction {

	// dependencies

	@Inject
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
	Responder goReal () {

		List<?> objectIds =
			(List<?>)
			requestContext.session (
				sessionKey + "Results");

		if (objectIds == null) {

			return responder (
				searchResponderName);

		} else {

			return responder (
				searchResultsResponderName);

		}

	}

}
