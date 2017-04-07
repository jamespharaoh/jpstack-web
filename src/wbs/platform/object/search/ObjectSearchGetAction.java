package wbs.platform.object.search;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringSplitComma;

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.console.UserSessionLogic;
import wbs.platform.user.model.UserRec;

import wbs.utils.etc.NumberUtils;

import wbs.web.responder.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("objectSearchGetAction")
public
class ObjectSearchGetAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserSessionLogic userSessionLogic;

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
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goReal");

		try (

			Transaction transaction =
				database.beginReadWrite (
					"ObjectSearchGetAction.goReal",
					this);

		) {

			UserRec user =
				userConsoleLogic.userRequired ();

			Optional <Serializable> searchOptional =
				userSessionLogic.userDataObject (
					taskLogger,
					user,
					stringFormat (
						"object_search_%s_fields",
						sessionKey));

			Optional <List <Long>> objectIdsOptional =
				optionalMapRequired (
					userSessionLogic.userDataString (
						user,
						stringFormat (
							"object_search_%s_results",
							sessionKey)),
					objectIdStrings ->
						iterableMapToList (
							NumberUtils::parseIntegerRequired,
							stringSplitComma (
								objectIdStrings)));

			if (

				optionalIsNotPresent (
					searchOptional)

				|| optionalIsNotPresent (
					objectIdsOptional)

			) {

				userSessionLogic.userDataRemove (
					user,
					stringFormat (
						"object_search_%s_results",
						sessionKey));

				transaction.commit ();

				return responder (
					searchResponderName);

			} else {

				transaction.commit ();

				return responder (
					searchResultsResponderName);

			}

		}

	}

}
