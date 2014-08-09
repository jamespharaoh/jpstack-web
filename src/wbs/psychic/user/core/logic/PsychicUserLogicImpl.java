package wbs.psychic.user.core.logic;

import java.util.Random;

import javax.inject.Inject;

import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectManager;
import wbs.psychic.bill.logic.PsychicBillLogic;
import wbs.psychic.bill.model.PsychicBillMode;
import wbs.psychic.contact.logic.PsychicContactLogic;
import wbs.psychic.core.model.PsychicRec;
import wbs.psychic.core.model.PsychicSettingsRec;
import wbs.psychic.send.logic.PsychicSendLogic;
import wbs.psychic.user.core.model.PsychicUserDao;
import wbs.psychic.user.core.model.PsychicUserObjectHelper;
import wbs.psychic.user.core.model.PsychicUserRec;
import wbs.sms.number.core.model.NumberRec;

@SingletonComponent ("psychicUserLogic")
public
class PsychicUserLogicImpl
	implements PsychicUserLogic {

	@Inject
	ObjectManager objectManager;

	@Inject
	PsychicBillLogic psychicBillLogic;

	@Inject
	PsychicContactLogic psychicContactLogic;

	@Inject
	PsychicUserDao psychicUserDao;

	@Inject
	PsychicUserObjectHelper psychicUserHelper;

	@Inject
	PsychicSendLogic psychicSendLogic;

	@Inject
	Random random;

	@Override
	public
	PsychicUserRec findOrCreateUser (
			PsychicRec psychic,
			NumberRec number) {

		PsychicUserRec user =
			psychicUserHelper.find (
				psychic,
				number);

		PsychicSettingsRec settings =
			psychic.getSettings ();

		if (user == null) {

			user =
				objectManager.insert (
					new PsychicUserRec ()
						.setPsychic (psychic)
						.setPsychicAffiliate (
							settings.getDefaultPsychicAffiliate ())
						.setCode (generateUserCode (psychic))
						.setNumber (number)
						.setCreateTime (Instant.now ()));

			if (number.getFree ()) {

				user.getAccount ()
					.setBillMode (PsychicBillMode.free);

			}

		}

		return user;

	}

	String generateUserCode (
			PsychicRec psychic) {

		for (;;) {

			// generate a random code

			String code =
				Integer.toString (
					random.nextInt (900000) + 100000);

			// look for an existing user with that code

			PsychicUserRec psychicUser =
				psychicUserHelper.findByCode (
					psychic,
					code);

			// return it if there was no user

			if (psychicUser == null)
				return code;

		}

	}

	@Override
	public
	void join (
			PsychicUserRec user,
			Integer threadId) {

		// confirm charges

		if (! user.getChargesConfirmed ()) {

			psychicSendLogic.sendCharges (
				user,
				threadId);

			return;

		}

		// update join time

		user.setLastJoinTime (Instant.now ());

		// first joiners

		if (user.getFirstJoinTime () == null)
			firstJoin (user, threadId);

		// send a profile

		psychicContactLogic.sendProfile (
			user,
			threadId);

	}

	private void firstJoin (
			PsychicUserRec user,
			Integer threadId) {

		if (user.getFirstJoinTime () != null)
			throw new IllegalStateException ();

		// set their join time

		user.setFirstJoinTime (
			user.getLastJoinTime ());

		// give them some free credit

		psychicBillLogic.addInitialCredit (
			user);

		// send a welcome message

		psychicSendLogic.sendWelcome (
			user,
			threadId);

	}

}
