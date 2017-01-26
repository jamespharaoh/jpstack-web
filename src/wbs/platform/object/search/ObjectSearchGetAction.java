package wbs.platform.object.search;

import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.web.responder.Responder;

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

		Optional <Serializable> searchOptional =
			requestContext.session (
				sessionKey + "Fields");

		Optional <List <?>> objectIdsOptional =
			genericCastUnchecked (
				requestContext.session (
					sessionKey + "Results"));

		if (

			optionalIsNotPresent (
				searchOptional)

			|| optionalIsNotPresent (
				objectIdsOptional)

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
