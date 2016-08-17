package wbs.smsapps.forwarder.logic;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifElse;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.joda.time.Instant;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.GlobalId;
import wbs.platform.media.model.MediaRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.delivery.model.DeliveryTypeObjectHelper;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.message.wap.logic.WapPushLogic;
import wbs.sms.message.wap.model.WapPushMessageObjectHelper;
import wbs.sms.message.wap.model.WapPushMessageRec;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.logic.RouterLogic;
import wbs.sms.tracker.logic.SmsTrackerManager;
import wbs.smsapps.forwarder.model.ForwarderMessageInObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec;
import wbs.smsapps.forwarder.model.ForwarderMessageOutObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageOutRec;
import wbs.smsapps.forwarder.model.ForwarderRec;
import wbs.smsapps.forwarder.model.ForwarderRouteObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderRouteRec;

@Log4j
@SingletonComponent ("forwarderLogic")
public
class ForwarderLogicImplementation
	implements ForwarderLogic {

	// dependencies

	@Inject
	DeliveryTypeObjectHelper deliveryTypeHelper;

	@Inject
	ForwarderMessageInObjectHelper forwarderMessageInHelper;

	@Inject
	ForwarderMessageOutObjectHelper forwarderMessageOutHelper;

	@Inject
	ForwarderRouteObjectHelper forwarderRouteHelper;

	@Inject
	NetworkObjectHelper networkHelper;

	@Inject
	NumberObjectHelper numberHelper;

	@Inject
	RouterLogic routerLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	SmsTrackerManager smsTrackerManager;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	WapPushMessageObjectHelper wapPushMessageHelper;

	@Inject
	WapPushLogic wapPushLogic;

	// prototype dependencies

	@Inject
	Provider<MessageSender> messageSender;

	// implementation

	public static
	class SendTemplate {

		public
		ForwarderRec forwarder;

		public
		Long fmInId;

		public
		ForwarderMessageInRec fmIn;

		public
		List<SendPart> parts =
			new ArrayList<SendPart> ();

		public
		List<String> errors =
			new ArrayList<String> ();

		public
		SendError sendError;

	}

	public static
	class SendPart {

		public
		String message;

		public
		String url;

		public
		String numFrom;

		public
		String numTo;

		public
		NumberRec numToNumber;

		public
		String routeCode;

		public
		ForwarderRouteRec forwarderRoute;

		public
		String serviceCode;

		public
		ServiceRec service;

		public
		Long networkId;

		public
		NetworkRec network;

		public
		String clientId;

		public
		Long pri;

		public
		Long retryDays;

		public
		Set<String> tags;

		public
		Collection<MediaRec> medias;

		public
		String subject;

		public
		ForwarderMessageOutRec forwarderMessageOut;

		public
		List<String> errors =
			new ArrayList<String> ();

		public
		SendError sendError;

	}

	public static
	enum SendError {
		missingReplyToMessageId,
		tooManyReplies,
		invalidReplyToMessageId,
		invalidRoute,
		invalidNetworkId,
		invalidNumFrom,
		missingClientId,
		reusedClientId,
		trackerBlocked
	}

	private static
	class SendTemplateCheckWork {

		SendTemplate template;

		boolean ret = true;

		int freeParts = 0;
		int billParts = 0;
		int unknownParts = 0;

		boolean sentSome = false;
		boolean notSentSome = false;

	}

	@Override
	public
	boolean sendTemplateCheck (
			SendTemplate template) {

		SendTemplateCheckWork work =
			new SendTemplateCheckWork ();

		work.template = template;

		if (template.forwarder == null)
			throw new NullPointerException ("template.forwarder not supplied");

		// lookup forwarderMessageIn

		sendTemplateCheckFmIn (work);

		// check reply to message id

		if (! work.template.forwarder.getAllowNewSends ()
				&& template.fmIn == null) {

			log.debug ("no reply-to on template : " + template.toString ());

			work.template.errors.add (
				"Reply-to-message-id must be specified");

			if (work.template.sendError == null)
				template.sendError = SendError.missingReplyToMessageId;

			work.ret = false;

		}

		// check parts

		sendTemplateCheckParts (work);

		// match against existing message

		sendTemplateCheckExisting (work);

		// check counters (if necessary)

		if (work.notSentSome && template.fmIn != null) {

			if (template.fmIn.getBillRepliesSent ()
					+ template.fmIn.getFreeRepliesSent ()
					+ work.freeParts
					+ work.billParts
					+ work.unknownParts
					> template.forwarder.getMaxReplies ()) {

				template.errors.add (
					"Too many replies to this message");

				if (work.template.sendError == null)
					template.sendError = SendError.tooManyReplies;

				work.ret = false;

			}

			if (template.fmIn.getBillRepliesSent ()
					+ work.billParts
					> template.forwarder.getMaxBillReplies ()) {

				template.errors.add (
					"Too many billed replies to this message");

				if (work.template.sendError == null)
					template.sendError = SendError.tooManyReplies;

				work.ret = false;

			}

			if (template.fmIn.getFreeRepliesSent ()
					+ work.freeParts
					> template.forwarder.getMaxFreeReplies ()) {

				template.errors.add (
					"Too many free replies to this message");

				if (work.template.sendError == null)
					template.sendError = SendError.tooManyReplies;

				work.ret = false;

			}

		}

		// return

		return work.ret;

	}

	private
	void sendTemplateCheckFmIn (
			SendTemplateCheckWork work) {

		int i =
			+ (work.template.fmInId != null ? 1 : 0)
			+ (work.template.fmIn != null ? 1 : 0);

		if (i > 1)
			throw new RuntimeException (
				"Specified both forwarderMessageIn and fmInId");

		if (work.template.fmInId != null) {

			Optional<ForwarderMessageInRec> messageInOptional =
				forwarderMessageInHelper.find (
					work.template.fmInId);

			if (
				isNotPresent (
					messageInOptional)
			) {

				work.template.errors.add (
					"Reply-to-message-id is invalid: " +
					work.template.fmInId);

				if (work.template.sendError == null) {

					work.template.sendError =
						SendError.invalidReplyToMessageId;

				}

				work.ret = false;

			}

			work.template.fmIn =
				messageInOptional.get ();

		}

	}

	private
	void sendTemplateCheckParts (
			@NonNull SendTemplateCheckWork work) {

		for (
			SendPart part
				: work.template.parts
		) {

			// lookup route

			if ((part.routeCode != null ? 1 : 0)
					+ (part.forwarderRoute != null ? 1 : 0)
					!= 1) {
				throw new RuntimeException ();

			} else if (part.routeCode != null) {

				Optional<ForwarderRouteRec> forwarderRouteOptional =
					forwarderRouteHelper.findByCode (
						work.template.forwarder,
						part.routeCode);

				if (
					isNotPresent (
						forwarderRouteOptional)
				) {

					sendTemplateCheckError (
						work,
						part,
						SendError.invalidRoute,
						"Route not recognised: " + part.routeCode);

				}

				part.forwarderRoute =
					forwarderRouteOptional.get ();

				part.routeCode = null;

			}

			// check route is set up

			if (
				part.forwarderRoute != null
				&& part.forwarderRoute.getRouter () == null
			) {

				sendTemplateCheckError (
					work,
					part,
					SendError.invalidRoute,
					"Route not configured correctly: " + part.forwarderRoute.getCode ());

			}

			// check route/number

			if (part.forwarderRoute != null
					&& part.forwarderRoute.getNumber ().length () > 0
					&& ! equal (
						part.forwarderRoute.getNumber (),
						part.numFrom)) {

				sendTemplateCheckError (
					work,
					part,
					SendError.invalidNumFrom,
					"Number is not valid for route: " + part.numFrom);

			}

			// check service

			int serviceCount =
				+ (part.serviceCode != null ? 1 : 0)
				+ (part.service != null ? 1 : 0);

			if (serviceCount == 0)
				throw new RuntimeException (
					"Must specify serviceCode or service (not neither)");

			if (serviceCount > 1)
				throw new RuntimeException (
					"Must specify serviceCode or service (not both)");

			if (part.serviceCode != null) {

				part.service =
					serviceHelper.findOrCreate (
						work.template.forwarder,
						"default",
						part.serviceCode);

			}

			// check network
			int networkCount = (part.networkId != null ? 1 : 0)
					+ (part.network != null ? 1 : 0);

			if (networkCount > 1) {

				throw new RuntimeException (
					"Cannot specify networkId and network");

			}

			if (
				isNotNull (
					part.networkId)
			) {

				if (part.networkId == 0) {

					sendTemplateCheckError (
						work,
						part,
						SendError.invalidNetworkId,
						"Network ID 0 should not be specified");

				}

				Optional<NetworkRec> networkOptional =
					networkHelper.find (
						part.networkId);

				if (
					isNotPresent (
						networkOptional)
				) {

					sendTemplateCheckError (
						work,
						part,
						SendError.invalidNetworkId,
						"Network ID not recognised: " + part.networkId);

				}

				part.network =
					networkOptional.get ();

			}

			// check client-id

			if (! work.template.forwarder.getAllowNullOtherId ()
					&& part.clientId == null) {

				sendTemplateCheckError (
					work,
					part,
					SendError.missingClientId,
					"Client ID must be supplied");

			}

			// check tracker

			if (work.template.forwarder.getSmsTracker() != null) {

				part.numToNumber =
					numberHelper.findOrCreate (
						part.numTo);

				if (
					! smsTrackerManager.canSend (
						work.template.forwarder.getSmsTracker (),
						part.numToNumber,
						Optional.<Instant>absent ())
				) {

					sendTemplateCheckError (
						work,
						part,
						SendError.trackerBlocked,
						"Messages to this number are blocked");

				}

			}

			// inc counters

			if (part.forwarderRoute != null) {

				RouteRec route =
					routerLogic.resolveRouter (
						part.forwarderRoute.getRouter ());

				if (route.getOutCharge () > 0) {

					work.billParts ++;

				} else {

					work.freeParts ++;

				}

			} else {

				work.unknownParts ++;

			}

		}

	}

	private
	void sendTemplateCheckError (
			SendTemplateCheckWork work,
			SendPart sendPart,
			SendError sendError,
			String messageText) {

		sendPart.errors.add (messageText);

		if (sendPart.sendError == null)
			sendPart.sendError = sendError;

		if (work.template.sendError == null)
			work.template.sendError = sendError;

		work.ret = false;

	}

	private
	void sendTemplateCheckExisting (
			SendTemplateCheckWork work) {

		// check if any unqueueExMessages have already been sent

		for (
			SendPart sendPart
				: work.template.parts
		) {

			sendPart.forwarderMessageOut =
				findExistingForwarderMessageOut (
					work.template.forwarder,
					work.template.fmIn,
					sendPart.message,
					sendPart.url,
					sendPart.numFrom,
					sendPart.numTo,
					sendPart.forwarderRoute,
					sendPart.clientId,
					sendPart.pri);

			if (sendPart.forwarderMessageOut != null) {

				work.sentSome = true;

			} else {

				work.notSentSome = true;

			}

		}

		// if some of these unqueueExMessages already exist but some don't then stop

		if (work.sentSome && work.notSentSome) {

			work.template.errors.add(
				"Previously sent unqueueExMessages being resent in different groups (very strange!)");

			if (work.template.sendError == null)
				work.template.sendError = SendError.reusedClientId;

			work.ret = false;

		}

		// if all of them are previously sent, check the group is the same and
		// if so return the old result

		if (work.sentSome && !work.notSentSome) {

			for (int i = 0; i < work.template.parts.size() - 1; i++) {

				if (work.template.parts.get(i).forwarderMessageOut
						.getNextForwarderMessageOut() != work.template.parts
						.get(i + 1).forwarderMessageOut) {

					work.template.errors.add(
						"Previously sent unqueueExMessages being resent in different groups (very strange!)");

					if (work.template.sendError == null)
						work.template.sendError = SendError.reusedClientId;

					work.ret = false;

				}

			}

		}

	}

	@Override
	public
	void sendTemplateSend (
			SendTemplate sendTemplate) {

		ForwarderMessageOutRec lastForwarderMessageOut = null;

		Long threadId =
			sendTemplate.fmIn != null
				? sendTemplate.fmIn.getMessage ().getThreadId ()
				: null;

		// for each part

		for (
			SendPart sendPart
				: sendTemplate.parts
		) {

			// check it is still going

			if (sendPart.forwarderMessageOut != null)
				continue;

			// send it

			sendPart.forwarderMessageOut =
				createMessage (
					sendTemplate.forwarder,
					sendTemplate.fmIn,
					sendPart.message,
					sendPart.url,
					sendPart.numFrom,
					sendPart.numTo,
					threadId,
					sendPart.forwarderRoute,
					sendPart.service,
					sendPart.clientId,
					sendPart.pri,
					lastForwarderMessageOut == null,
					sendPart.tags,
					sendPart.network,
					sendPart.subject,
					sendPart.medias);

			// save the thread id if we haven't got one yet

			if (threadId == null)
				threadId = sendPart.forwarderMessageOut.getMessage().getThreadId();

			// link unqueueExMessages where appropriate

			if (lastForwarderMessageOut != null) {

				lastForwarderMessageOut

					.setNextForwarderMessageOut (
						sendPart.forwarderMessageOut);

				sendPart.forwarderMessageOut

					.setPrevForwarderMessageOut (
						lastForwarderMessageOut);

			}

			lastForwarderMessageOut =
				sendPart.forwarderMessageOut;

		}

	}

	@Override
	public
	ForwarderMessageOutRec sendMessage (
			ForwarderRec forwarder,
			ForwarderMessageInRec fmIn,
			String message,
			String url,
			String numFrom,
			String numTo,
			String routeCode,
			String myId,
			Long pri,
			Collection<MediaRec> medias) {

		SendTemplate sendTemplate =
			new SendTemplate ();

		sendTemplate.forwarder = forwarder;
		sendTemplate.fmIn = fmIn;

		SendPart sendPart =
			new SendPart ();

		sendTemplate.parts.add (
			sendPart);

		sendPart.message = message;
		sendPart.url = url;
		sendPart.numFrom = numFrom;
		sendPart.numTo = numTo;
		sendPart.routeCode = routeCode;
		sendPart.serviceCode = "default";
		sendPart.clientId = myId;
		sendPart.pri = pri;
		sendPart.medias = medias;

		if (! sendTemplateCheck (sendTemplate)) {

			if (sendTemplate.sendError == SendError.trackerBlocked)
				return null;
			if (sendTemplate.errors.size() > 0)
				throw new RuntimeException(sendTemplate.errors.get(0));
			else
				throw new RuntimeException(sendTemplate.parts.get(0).errors.get(0));

		}

		sendTemplateSend (
			sendTemplate);

		return sendTemplate.parts.get (0).forwarderMessageOut;

	}

	private
	ForwarderMessageOutRec findExistingForwarderMessageOut (
			ForwarderRec forwarder,
			ForwarderMessageInRec forwarderMessageIn,
			String messageText,
			String url,
			String numFrom,
			String numTo,
			ForwarderRouteRec route,
			String otherId,
			Long priority) {

		// if there is no client id, skip this check

		if (otherId == null)
			return null;

		// look up any existing message

		ForwarderMessageOutRec forwarderMessaeOut =
			forwarderMessageOutHelper.findByOtherId (
				forwarder,
				otherId);

		if (forwarderMessaeOut == null)
			return null;

		// make sure the message matches

		MessageRec message =
			forwarderMessaeOut.getMessage ();

		WapPushMessageRec wapPushMessage =
			ifElse (
				isNotNull (
					url),
				() -> wapPushMessageHelper.findRequired (
					message.getId ()),
				() -> null);

		if (

			notEqual (
				forwarderMessaeOut.getForwarderMessageIn (),
				forwarderMessageIn)

			|| notEqual (
				message.getNumFrom (),
				numFrom)

			|| notEqual (
				message.getNumTo (),
				numTo)

			|| notEqual (
				forwarderMessaeOut.getForwarderRoute (),
				route)

			|| notEqual (
				message.getPri (),
				priority)

			|| (

				url == null

				&& (
					notEqual (
						message.getMessageType ().getCode (),
						"sms")
					|| notEqual (
						message.getText ().getText (),
						messageText)
				)

			) || (

				url != null

				&& (

					notEqual (
						message.getMessageType ().getCode (),
						"wap_push")

					|| notEqual (
						wapPushMessage.getTextText ().getText (),
						messageText)

					|| notEqual (
						wapPushMessage.getUrlText ().getText (),
						url)

				)

			)

		) {

			throw new ForwarderSendClientIdException (
				"Client message ID reused: " + otherId + " forwarder "
				+ forwarder.getId ());

		}

		// return

		return forwarderMessaeOut;

	}

	private
	ForwarderMessageOutRec createMessage (
			ForwarderRec forwarder,
			ForwarderMessageInRec fmIn,
			String message,
			String url,
			String numfrom,
			String numto,
			Long threadId,
			ForwarderRouteRec forwarderRoute,
			ServiceRec service,
			String myId,
			Long pri,
			boolean sendNow,
			Set<String> tags,
			NetworkRec network,
			String subject,
			Collection<MediaRec> medias) {

		// lookup some stuff

		NumberRec number =
			numberHelper.findOrCreate (
				numto);

		if (threadId == null && fmIn != null)
			threadId = fmIn.getMessage().getThreadId();

		// create the out record

		ForwarderMessageOutRec forwarderMessageOut =
			forwarderMessageOutHelper.insert (
				forwarderMessageOutHelper.createInstance ()

			.setForwarder (
				forwarder)

			.setForwarderMessageIn (
				fmIn)

			.setNumber (
				number)

			.setOtherId (
				myId)

			.setBill (
				false)

			.setForwarderRoute (
				forwarderRoute)

		);

		// send the message

		MessageRec messageOut;

		if (url == null) {

			messageOut =
				messageSender.get ()

				.threadId (
					threadId)

				.number (
					number)

				.messageString (
					message)

				.numFrom (
					numfrom)

				.routerResolve (
					forwarderRoute.getRouter ())

				.service (
					service)

				.deliveryTypeCode (
					"forwarder")

				.ref (
					forwarderMessageOut.getId ())

				.subjectString (
					subject)

				.medias (
					medias)

				.sendNow (
					sendNow)

				.tags (
					tags)

				.network (
					network)

				.send ();

		} else {

			messageOut =
				wapPushLogic.wapPushSend (
					threadId,
					number,
					numfrom,
					textHelper.findOrCreate (message),
					textHelper.findOrCreate (url),
					forwarderRoute.getRouter (),
					service,
					null,
					null,
					deliveryTypeHelper.findByCodeRequired (
						GlobalId.root,
						"forwarder"),
					forwarderMessageOut.getId (),
					sendNow,
					tags,
					network);
		}

		forwarderMessageOut.setMessage (messageOut);

		return forwarderMessageOut;

	}

}
