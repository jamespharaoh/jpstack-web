package wbs.psychic.contact.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectManager;
import wbs.framework.utils.StringSubstituter;
import wbs.psychic.contact.model.PsychicContactRec;
import wbs.psychic.profile.model.PsychicProfileObjectHelper;
import wbs.psychic.profile.model.PsychicProfileRec;
import wbs.psychic.send.logic.PsychicSendLogic;
import wbs.psychic.template.model.PsychicTemplateObjectHelper;
import wbs.psychic.template.model.PsychicTemplateRec;
import wbs.psychic.user.core.model.PsychicUserRec;

@SingletonComponent ("psychicContactLogic")
public
class PsychicContactLogicImpl
	implements PsychicContactLogic {

	@Inject
	ObjectManager objectManager;

	@Inject
	PsychicProfileObjectHelper psychicProfileHelper;

	@Inject
	PsychicSendLogic psychicSendLogic;

	@Inject
	PsychicTemplateObjectHelper psychicTemplateHelper;

	@Inject
	Random random;

	@Override
	public
	void sendProfile (
			PsychicUserRec user,
			Integer threadId) {

		// select the profile contact

		PsychicContactRec contact =
			selectContact (user);

		if (contact == null)
			throw new RuntimeException ();

		// update contact

		Instant now =
			Instant.now ();

		if (contact.getFirstProfile () == null)
			contact.setFirstProfile (now);

		contact.setLastProfile (now);

		contact.setNumProfiles (
			contact.getNumProfiles () + 1);

		// send the message

		sendProfile (
			user,
			contact.getPsychicProfile (),
			threadId);

	}

	@Override
	public
	void sendProfile (
			PsychicUserRec user,
			PsychicProfileRec profile,
			Integer threadId) {

		PsychicTemplateRec profileTemplate =
			psychicTemplateHelper.findByCode (
				user.getPsychic (),
				"profile");

		String messageBody =
			new StringSubstituter ()

				.param (
					"name",
					profile.getName ())

				.param (
					"info",
					profile.getInfo ())

				.substitute (
					profileTemplate
						.getTemplateText ()
						.getText ());

		psychicSendLogic.sendMagic (
			user,
			"send_to_profile",
			profile.getId (),
			threadId,
			messageBody,
			"profile");

	}

	@Override
	public
	PsychicContactRec selectContact (
			PsychicUserRec psychicUser) {

		// randomly add unseen profiles to the list while between runs

		if (psychicUser.getNextContactIndex () == null) {

			List<PsychicProfileRec> allPsychicProfiles =
				psychicProfileHelper.findByParent (
					psychicUser.getPsychic ());

			List<PsychicProfileRec> unseenPsychicProfiles =
				new ArrayList<PsychicProfileRec> ();

			for (PsychicProfileRec psychicProfile
					: allPsychicProfiles) {

				PsychicContactRec psychicContact =
					psychicUser.getContactsByProfileId ().get (
						psychicProfile.getId ());

				if (psychicContact == null)
					unseenPsychicProfiles.add (psychicProfile);

			}

			if (! unseenPsychicProfiles.isEmpty ()) {

				PsychicProfileRec psychicProfile =
					unseenPsychicProfiles.get (
						random.nextInt (unseenPsychicProfiles.size ()));

				PsychicContactRec psychicContact =
					findOrCreatePsychicContact (
						psychicUser,
						psychicProfile);

				return psychicContact;

			}

			psychicUser.setNextContactIndex (0);

		}

		// return next profile in sequence

		PsychicContactRec contact =
			psychicUser.getContactsByIndex ().get (
				psychicUser.getNextContactIndex ());

		if (psychicUser.getContactsByIndex ().get (
				psychicUser.getNextContactIndex () + 1) != null) {

			psychicUser.setNextContactIndex (
				psychicUser.getNextContactIndex () + 1);

		} else {

			psychicUser.setNextContactIndex (null);

		}

		return contact;

	}

	@Override
	public
	PsychicContactRec findOrCreatePsychicContact (
			PsychicUserRec user,
			PsychicProfileRec profile) {

		PsychicContactRec contact =
			user.getContactsByProfileId ()
				.get (profile.getId ());

		if (contact != null)
			return contact;

		contact =
			objectManager.insert (
				new PsychicContactRec ()
					.setPsychicUser (user)
					.setIndex (user.getContactsByIndex ().size ())
					.setPsychicProfile (profile));

		user.getContactsByIndex ().put (
			contact.getIndex (),
			contact);

		user.getContactsByProfileId ().put (
			profile.getId (),
			contact);

		return contact;

	}

}
