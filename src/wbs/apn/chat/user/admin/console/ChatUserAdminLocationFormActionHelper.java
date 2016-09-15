package wbs.apn.chat.user.admin.console;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.apn.chat.user.admin.console.ChatUserAdminLocationFormActionHelper.ChatUserAdminLocationForm;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.message.core.model.MessageRec;

@PrototypeComponent ("chatUserAdminLocationFormActionHelper")
public
class ChatUserAdminLocationFormActionHelper
	extends AbstractConsoleFormActionHelper <ChatUserAdminLocationForm> {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// implementation

	@Override
	public
	ChatUserAdminLocationForm constructFormState () {

		return new ChatUserAdminLocationForm ();

	}

	@Override
	public
	void updatePassiveFormState (
			@NonNull ChatUserAdminLocationForm formState) {

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

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

	@Override
	public
	Optional<Responder> processFormSubmission (
			@NonNull Transaction transaction,
			@NonNull ChatUserAdminLocationForm formState) {

		// check form is filled in ok

		if (formState.newLocationName () == null) {

			requestContext.addWarning (
				"Please specify a location");

			return Optional.<Responder>absent ();

		}

		// perform update

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

		boolean success =
			chatUserLogic.setPlace (
				chatUser,
				formState.newLocationName (),
				Optional.<MessageRec>absent (),
				Optional.of (
					userConsoleLogic.userRequired ()));

		// handle location not found

		if (! success) {

			requestContext.addError (
				"That location could not be found");

			return Optional.<Responder>absent ();

		}

		// finish

		transaction.commit ();

		requestContext.addNotice (
			"Location updated successfully");

		return Optional.<Responder>absent ();

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
