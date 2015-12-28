package wbs.platform.object.search;

import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

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

		Object searchObject =
			requestContext.session (
				sessionKey + "Fields");

		List<?> objectIds =
			(List<?>)
			requestContext.session (
				sessionKey + "Results");

System.out.println (
	stringFormat (
		"X0 %s, %s",
		searchObject == null ? "NULL" : "NOT NULL",
		objectIds == null ? "NULL" : "NOT NULL"));

		if (

			isNull (
				searchObject)

			|| isNull (
				objectIds)

		) {

			requestContext.session (
				sessionKey + "Results",
				null);

System.out.println ("X1");

			return responder (
				searchResponderName);

		} else {

System.out.println ("X2 " + objectIds.size ());

			return responder (
				searchResultsResponderName);

		}

	}

}
