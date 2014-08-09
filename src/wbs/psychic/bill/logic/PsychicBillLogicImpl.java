package wbs.psychic.bill.logic;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import org.joda.time.LocalDate;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.psychic.affiliate.model.PsychicAffiliateRec;
import wbs.psychic.bill.model.PsychicBillMode;
import wbs.psychic.bill.model.PsychicChargesRec;
import wbs.psychic.bill.model.PsychicUserAccountRec;
import wbs.psychic.core.model.PsychicRec;
import wbs.psychic.core.model.PsychicRoutesRec;
import wbs.psychic.send.logic.PsychicSendLogic;
import wbs.psychic.template.model.PsychicTemplateObjectHelper;
import wbs.psychic.template.model.PsychicTemplateRec;
import wbs.psychic.user.core.model.PsychicUserRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.MessageSender;

@SingletonComponent ("psychicBillLogic")
public
class PsychicBillLogicImpl
	implements PsychicBillLogic {

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	PsychicSendLogic psychicSendLogic;

	@Inject
	PsychicTemplateObjectHelper psychicTemplateHelper;

	@Inject
	Provider<MessageSender> messageSender;

	@Override
	public
	CreditResult creditCheck (
			PsychicUserRec psychicUser,
			int amount,
			boolean chargeNow) {

		PsychicRec psychic =
			psychicUser.getPsychic ();

		PsychicChargesRec charges =
			psychic.getCharges ();

		PsychicRoutesRec routes =
			psychic.getRoutes ();

		PsychicUserAccountRec userAccount =
			psychicUser.getAccount ();

		// stopped user, always false

		if (psychicUser.getStopped ())
			return CreditResult.stopped;

		// free user, always true

		if (userAccount.getBillMode () == PsychicBillMode.free)
			return CreditResult.passed;

		// barred user, always false

		if (userAccount.getBillMode () == PsychicBillMode.barred)
			return CreditResult.barred;

		// work out user's effective credit

		int effectiveCredit =
			+ userAccount.getCreditFree ()
			+ userAccount.getCreditPending ()
			+ userAccount.getCreditSuccess ()
			+ userAccount.getCreditAdminPaid ()
			+ userAccount.getCreditAdminFree ()
			- userAccount.getRequestSpend ();

		// already in credit, return true

		if (effectiveCredit >= amount)
			return CreditResult.passed;

		// can't charge and no credit, return false

		if (! chargeNow)
			return CreditResult.failed;

		// prepay and no credit, return false

		if (userAccount.getBillMode () == PsychicBillMode.prepay)
			return CreditResult.prepay;

		// reached credit limit, return false

		if (userAccount.getCreditPending () >= charges.getCreditLimit ())
			return CreditResult.limit;

		// reached daily bill limit, return false

		LocalDate today =
			LocalDate.now ();

		if (equal (userAccount.getDailyDate (), today)
			&& (userAccount.getDailyAmount ()
				+ routes.getBillRoute ().getOutCharge ())
				> charges.getDailyLimit ())
			return CreditResult.daily;

		// send a billed message, return true

		sendBilledMessage (psychicUser);

		return CreditResult.passed;

	}

	@Override
	public
	boolean chargeOneRequest (
			PsychicUserRec psychicUser,
			Integer threadId) {

		PsychicRec psychic =
			psychicUser.getPsychic ();

		PsychicChargesRec charges =
			psychic.getCharges ();

		PsychicUserAccountRec account =
			psychicUser.getAccount ();

		// perform a credit check

		CreditResult result =
			creditCheck (
				psychicUser,
				charges.getRequestCharge (),
				true);

		switch (result) {

		case barred:

			// barred users are ignored

			return false;

		case stopped:

			// should be impossible

			throw new RuntimeException (
				stringFormat (
					"Attempt to charge stopped user %s",
					psychicUser.getId ()));

		case daily:

			// daily limit reached, send message

			PsychicTemplateRec dailyLimitTemplate =
				psychicTemplateHelper.findByCode (
					psychic,
					"daily_limit_reached");

			psychicSendLogic.sendMagic (
				psychicUser,
				"help",
				0,
				threadId,
				dailyLimitTemplate.getTemplateText ().getText (),
				"default");

			return false;

		case limit:

			// credit limit reached, send message

			PsychicTemplateRec creditLimitReachedTemplate =
				psychicTemplateHelper.findByCode (
					psychic,
					"credit_limit_reached");

			psychicSendLogic.sendMagic (
				psychicUser,
				"help",
				0,
				threadId,
				creditLimitReachedTemplate.getTemplateText ().getText (),
				"default");

			return false;

		case prepay:

			// prepaid credit exhausted, send message

			PsychicTemplateRec prepayCreditExhaustedTemplate =
				psychicTemplateHelper.findByCode (
					psychic,
					"prepay_credit_exhausted");

			psychicSendLogic.sendMagic (
				psychicUser,
				"help",
				0,
				threadId,
				prepayCreditExhaustedTemplate.getTemplateText ().getText (),
				"default");

			return false;

		case passed:

			// credit check passed, charge one request

			account.setRequestCount (
				account.getRequestCount () + 1);

			account.setRequestSpend (
				account.getRequestSpend ()
				+ charges.getRequestCharge ());

			return true;

		default:
			throw new RuntimeException ();

		}

	}

	@Override
	public
	void addInitialCredit (
			PsychicUserRec psychicUser) {

		PsychicUserAccountRec account =
			psychicUser.getAccount ();

		PsychicRec psychic =
			psychicUser.getPsychic ();

		PsychicChargesRec charges =
			psychic.getCharges ();

		account.setCreditFree (
			+ account.getCreditFree ()
			+ charges.getInitialCredit ());

	}

	@Override
	public
	MessageRec sendBilledMessage (
			PsychicUserRec psychicUser) {

		PsychicUserAccountRec account =
			psychicUser.getAccount ();

		PsychicRec psychic =
			psychicUser.getPsychic ();

		PsychicRoutesRec routes =
			psychic.getRoutes ();

		ServiceRec service =
			serviceHelper.findByCode (psychic, "bill");

		PsychicAffiliateRec psychicAffiliate =
			psychicUser.getPsychicAffiliate ();

		AffiliateRec affiliate =
			affiliateHelper.findByCode (
				psychicAffiliate,
				"default");

		// sanity check

		if (account.getBillMode () != PsychicBillMode.normal)
			throw new RuntimeException ();

		// send the message

		PsychicTemplateRec billTemplate =
			psychicTemplateHelper.findByCode (
				psychic,
				"bill");

		MessageRec message =
			messageSender.get ()
				.number (psychicUser.getNumber ())
				.messageText (billTemplate.getTemplateText ())
				.numFrom (routes.getBillNumber ())
				.route (routes.getBillRoute ())
				.service (service)
				.affiliate (affiliate)
				.deliveryTypeCode ("psychic_bill")
				.ref (psychicUser.getId ())
				.send ();

		// update his account

		account.setCreditPending (
			account.getCreditPending ()
			+ routes.getBillRoute ().getOutCharge ());

		return message;

	}

}
