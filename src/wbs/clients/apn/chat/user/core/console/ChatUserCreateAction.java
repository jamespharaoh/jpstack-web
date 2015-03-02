package wbs.clients.apn.chat.user.core.console;

import javax.inject.Inject;

import lombok.Cleanup;

import org.joda.time.LocalDate;

import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.gazetteer.model.GazetteerEntryObjectHelper;
import wbs.sms.gazetteer.model.GazetteerEntryRec;
import wbs.sms.gsm.Gsm;

@PrototypeComponent ("chatUserCreateAction")
public
class ChatUserCreateAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	EventLogic eventLogic;

	@Inject
	Database database;

	@Inject
	GazetteerEntryObjectHelper gazetteerEntryHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("chatUserCreateResponder");
	}

	// implementation

	@Override
	protected
	Responder goReal () {

		// get and check name parameter

		String name =
			requestContext.parameter ("name");

		if (name.length() == 0) {
			requestContext.addError("Name is too short");
			return null;
		}

		if (!Gsm.isGsm(name)) {
			requestContext.addError("Name contains invalid characters");
			return null;
		}

		if (Gsm.length(name) > 16) {
			requestContext.addError("Name is too long");
			return null;
		}

		// get and check info parameter
		String info = requestContext.parameter("info");
		if (info.length() == 0) {
			requestContext.addError("Info is too short");
			return null;
		}
		if (!Gsm.isGsm(info)) {
			requestContext.addError("Info contains invalid characters");
			return null;
		}
		if (Gsm.length(info) > 160) {
			requestContext.addError("Info is too long");
			return null;
		}

		// get and check postcode

		String postcode =
			Gsm.toSimpleAlpha (
				requestContext.parameter ("postcode"));

		if (postcode.length() == 0) {
			requestContext.addError("Please specify a postcode");
			return null;
		}

		// get and check gender parameter

		String genderStr =
			requestContext.parameter ("gender");

		Gender gender;

		if (genderStr.equals("m"))
			gender = Gender.male;
		else if (genderStr.equals("f"))
			gender = Gender.female;
		else {
			requestContext.addError("Please select a gender");
			return null;
		}

		// get and check orient parameter
		String orientStr = requestContext.parameter("orient");
		Orient orient;
		if (orientStr.equals("g"))
			orient = Orient.gay;
		else if (orientStr.equals("s"))
			orient = Orient.straight;
		else if (orientStr.equals("b"))
			orient = Orient.bi;
		else {
			requestContext.addError("Please select an orient");
			return null;
		}

		// dob param

		String dateOfBirthString =
			requestContext.parameter ("dob");

		LocalDate dateOfBirth = null;

		if (dateOfBirthString.length () > 0) {

			try {

				dateOfBirth =
					LocalDate.parse (dateOfBirthString);

			} catch (Exception exception) {

				requestContext.addError (
					"Invalid date of birth, please use yyyy-mm-dd format");

			}

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		// get database objects

		ChatRec chat =
			chatHelper.find (
				requestContext.stuffInt ("chatId"));

		// lookup location

		GazetteerEntryRec gazetteerEntry =
			gazetteerEntryHelper.findByCode (
				chat.getGazetteer (),
				postcode);

		if (gazetteerEntry == null) {
			requestContext.addError ("Postcode not found");
			return null;
		}

		// create monitor

		ChatUserRec chatUser =
			chatUserLogic.createChatMonitor (chat)

			.setName (
				name)

			.setInfoText (
				textHelper.findOrCreate (info))

			.setLocPlace (
				postcode)

			.setLocPlaceLongLat (
				gazetteerEntry.getLongLat ())

			.setLocLongLat (
				gazetteerEntry.getLongLat ())

			.setGender (
				gender)

			.setOrient (
				orient)

			.setDob (
				dateOfBirth);

		// create event

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		eventLogic.createEvent (
			"chat_user_monitor_created",
			myUser,
			chatUser,
			name,
			postcode,
			gender.toString (),
			orient.toString (),
			info);

		transaction.commit ();

		requestContext.addNotice ("Monitor created");
		requestContext.setEmptyFormData();

		return null;

	}

}
