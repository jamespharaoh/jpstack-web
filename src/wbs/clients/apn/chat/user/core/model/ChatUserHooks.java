package wbs.clients.apn.chat.user.core.model;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectHooks;
import wbs.framework.utils.RandomLogic;
import wbs.sms.gazetteer.model.GazetteerEntryObjectHelper;
import wbs.sms.gazetteer.model.GazetteerEntryRec;

@SingletonComponent ("chatUserHooks")
public
class ChatUserHooks
	implements ObjectHooks<ChatUserRec> {

	// dependencies

	@Inject
	RandomLogic randomLogic;

	// indirect dependencies

	@Inject
	Provider<GazetteerEntryObjectHelper> gazetteerEntryHelperProvider;

	// implementation

	@Override
	public
	void beforeInsert (
			@NonNull ChatUserRec chatUser) {

		// set code

		if (
			isNull (
				chatUser.getCode ())
		) {
			chatUser.setCode (
				randomLogic.generateNumericNoZero (6));
		}

		// set type

		if (
			isNull (
				chatUser.getType ())
		) {
			chatUser.setType (
				ChatUserType.monitor);
		}

		// set location place

		if (

			isNotNull (
				chatUser.getLocationPlace ())

			&& isNull (
				chatUser.getLocationPlaceLongLat ())

		) {

			GazetteerEntryObjectHelper gazetteerEntryHelper =
				gazetteerEntryHelperProvider.get ();

			GazetteerEntryRec gazetteerEntry =
				gazetteerEntryHelper.findByCode (
					chatUser.getChat ().getGazetteer (),
					chatUser.getLocationPlace ());

			chatUser

				.setLocationPlaceLongLat (
					gazetteerEntry.getLongLat ())

				.setLocationLongLat (
					gazetteerEntry.getLongLat ());

		}

	}

}
