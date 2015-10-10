package wbs.clients.apn.chat.user.admin.console;

import javax.inject.Inject;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import wbs.clients.apn.chat.user.admin.console.ChatUserAdminLocationFormActionHelper.ChatUserAdminLocationForm;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.core.model.MessageRec;

import com.google.common.base.Optional;

@PrototypeComponent ("chatUserAdminLocationFormActionHelper")
public
class ChatUserAdminLocationFormActionHelper
	extends AbstractConsoleFormActionHelper<ChatUserAdminLocationForm> {

	// dependencies

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
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
			ChatUserAdminLocationForm formState) {

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt (
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

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt (
					"chatUserId"));

		boolean success =
			chatUserLogic.setPlace (
				chatUser,
				formState.newLocationName (),
				Optional.<MessageRec>absent (),
				Optional.of (myUser));

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
