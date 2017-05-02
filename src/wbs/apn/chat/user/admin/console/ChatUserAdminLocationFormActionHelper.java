package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.formaction.AbstractConsoleFormActionHelper;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.apn.chat.user.admin.console.ChatUserAdminLocationFormActionHelper.ChatUserAdminLocationForm;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.Responder;

@SingletonComponent ("chatUserAdminLocationFormActionHelper")
public
class ChatUserAdminLocationFormActionHelper
	extends AbstractConsoleFormActionHelper <
		ChatUserAdminLocationForm,
		Object
	> {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// implementation

	@Override
	public
	void updatePassiveFormState (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserAdminLocationForm formState) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"updatePassiveFormState");

		) {

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			formState

				.currentLocationName (
					chatUser.getLocationPlace ())

				.currentLocationLongitude (
					chatUser.getLocationLongLat () != null
						? chatUser.getLocationLongLat ().longitude ()
						: null)

				.currentLocationLatitude (
					chatUser.getLocationLongLat () != null
						? chatUser.getLocationLongLat ().latitude ()
						: null);

		}

	}

	@Override
	public
	Optional <Responder> processFormSubmission (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserAdminLocationForm formState) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"processFormSubmission");

		) {

			// check form is filled in ok

			if (formState.newLocationName () == null) {

				requestContext.addWarning (
					"Please specify a location");

				return optionalAbsent ();

			}

			// perform update

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			boolean success =
				chatUserLogic.setPlace (
					transaction,
					chatUser,
					formState.newLocationName (),
					optionalAbsent (),
					optionalOf (
						userConsoleLogic.userRequired (
							transaction)));

			// handle location not found

			if (! success) {

				requestContext.addError (
					"That location could not be found");

				return optionalAbsent ();

			}

			// finish

			transaction.commit ();

			requestContext.addNotice (
				"Location updated successfully");

			return optionalAbsent ();

		}

	}

	@Accessors (fluent = true)
	@Data
	public static
	class ChatUserAdminLocationForm {

		String currentLocationName;
		Double currentLocationLatitude;
		Double currentLocationLongitude;

		String newLocationName;

	}

}
