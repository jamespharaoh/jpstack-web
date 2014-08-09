package wbs.psychic.send.logic;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.psychic.affiliate.model.PsychicAffiliateRec;
import wbs.psychic.core.model.PsychicRec;
import wbs.psychic.core.model.PsychicRoutesRec;
import wbs.psychic.template.model.PsychicTemplateObjectHelper;
import wbs.psychic.template.model.PsychicTemplateRec;
import wbs.psychic.user.core.model.PsychicUserRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.magicnumber.logic.MagicNumberLogic;
import wbs.sms.message.core.model.MessageRec;

@SingletonComponent ("psychicSendLogic")
public
class PsychicSendLogicImpl
	implements PsychicSendLogic {

	// dependencies

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	MagicNumberLogic magicNumberLogic;

	@Inject
	PsychicTemplateObjectHelper psychicTemplateHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	void sendCharges (
			PsychicUserRec user,
			Integer threadId) {

		PsychicTemplateRec dobAndChargesTemplate =
			psychicTemplateHelper.findByCode (
				user.getPsychic (),
				"charges");

		sendMagic (
			user,
			"charges",
			0,
			threadId,
			dobAndChargesTemplate
				.getTemplateText ()
				.getText (),
			"default");

	}

	@Override
	public
	MessageRec sendWelcome (
			PsychicUserRec user,
			Integer threadId) {

		PsychicTemplateRec welcomeTemplate =
			psychicTemplateHelper.findByCode (
				user.getPsychic (),
				"welcome");

		PsychicAffiliateRec affiliate =
			user.getPsychicAffiliate ();

		String messageText =
			affiliate.getWelcomeMessage () != null
				? affiliate
					.getWelcomeMessage ()
				: welcomeTemplate
					.getTemplateText ()
					.getText ();

		return sendMagic (
			user,
			"help",
			0,
			threadId,
			messageText,
			"default");

	}

	@Override
	public
	MessageRec sendMagic (
			PsychicUserRec user,
			String commandCode,
			int magicRef,
			Integer threadId,
			String message,
			String serviceCode) {

		PsychicRec psychic =
			user.getPsychic ();

		PsychicRoutesRec routes =
			psychic.getRoutes ();

		PsychicAffiliateRec psychicAffiliate =
			user.getPsychicAffiliate ();

		AffiliateRec affiliate =
			affiliateHelper.findByCode (
				psychicAffiliate,
				"default");

		ServiceRec service =
			serviceHelper.findByCode (
				psychic,
				serviceCode);

		return magicNumberLogic.sendMessage (
			routes.getMagicNumberSet (),
			user.getNumber (),
			commandHelper.findByCode (psychic, commandCode),
			magicRef,
			threadId,
			textHelper.findOrCreate (message),
			routes.getMagicRouter (),
			service,
			null,
			affiliate);

	}

}
