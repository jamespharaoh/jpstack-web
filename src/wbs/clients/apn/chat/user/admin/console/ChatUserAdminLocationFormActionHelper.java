package wbs.clients.apn.chat.user.admin.console;

import javax.inject.Inject;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import wbs.clients.apn.chat.user.admin.console.ChatUserAdminLocationFormActionHelper.ChatUserAdminLocationForm;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.Responder;
import wbs.platform.console.request.ConsoleRequestContext;

import com.google.common.base.Optional;

@PrototypeComponent ("chatUserAdminLocationFormActionHelper")
public
class ChatUserAdminLocationFormActionHelper
	extends AbstractConsoleFormActionHelper<ChatUserAdminLocationForm> {

	// dependencies

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ConsoleRequestContext requestContext;

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
			@NonNull ChatUserAdminLocationForm formState) {

		requestContext.addNotice (
			"TODO this doesn't work yet");

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
