package wbs.smsapps.forwarder.logic;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerNotEqualSafe;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalNotEqualOrNotPresentWithClass;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;

import wbs.platform.media.model.MediaRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.delivery.model.DeliveryTypeObjectHelper;
import wbs.sms.message.outbox.logic.SmsMessageSender;
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

@SingletonComponent ("forwarderLogic")
public
class ForwarderLogicImplementation
	implements ForwarderLogic {

	// singleton dependencies

	@SingletonDependency
	DeliveryTypeObjectHelper deliveryTypeHelper;

	@SingletonDependency
	ForwarderMessageInObjectHelper forwarderMessageInHelper;

	@SingletonDependency
	ForwarderMessageOutObjectHelper forwarderMessageOutHelper;

	@SingletonDependency
	ForwarderRouteObjectHelper forwarderRouteHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NetworkObjectHelper networkHelper;

	@SingletonDependency
	NumberObjectHelper numberHelper;

	@SingletonDependency
	RouterLogic routerLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	SmsTrackerManager smsTrackerManager;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	WapPushMessageObjectHelper wapPushMessageHelper;

	@SingletonDependency
	WapPushLogic wapPushLogic;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <SmsMessageSender> messageSenderProvider;

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
			@NonNull Transaction parentTransaction,
			@NonNull SendTemplate template) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendTemplateCheck");

		) {

			SendTemplateCheckWork work =
				new SendTemplateCheckWork ();

			work.template = template;

			if (template.forwarder == null) {

				throw new NullPointerException (
					"template.forwarder not supplied");

			}

			// lookup forwarderMessageIn

			sendTemplateCheckFmIn (
				transaction,
				work);

			// check reply to message id

			if (! work.template.forwarder.getAllowNewSends ()
					&& template.fmIn == null) {

				transaction.debugFormat (
					"no reply-to on template: %s",
					template.toString ());

				work.template.errors.add (
					"Reply-to-message-id must be specified");

				if (work.template.sendError == null)
					template.sendError = SendError.missingReplyToMessageId;

				work.ret = false;

			}

			// check parts

			sendTemplateCheckParts (
				transaction,
				work);

			// match against existing message

			sendTemplateCheckExisting (
				transaction,
				work);

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

	}

	private
	void sendTemplateCheckFmIn (
			@NonNull Transaction parentTransaction,
			@NonNull SendTemplateCheckWork work) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendTemplateCheckFmIn");

		) {

			int i =
				+ (work.template.fmInId != null ? 1 : 0)
				+ (work.template.fmIn != null ? 1 : 0);

			if (i > 1)
				throw new RuntimeException (
					"Specified both forwarderMessageIn and fmInId");

			if (work.template.fmInId != null) {

				Optional <ForwarderMessageInRec> messageInOptional =
					forwarderMessageInHelper.find (
						transaction,
						work.template.fmInId);

				if (
					optionalIsNotPresent (
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

	}

	private
	void sendTemplateCheckParts (
			@NonNull Transaction parentTransaction,
			@NonNull SendTemplateCheckWork work) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendTemplateCheckParts");

		) {

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

					Optional <ForwarderRouteRec> forwarderRouteOptional =
						forwarderRouteHelper.findByCode (
							transaction,
							work.template.forwarder,
							part.routeCode);

					if (
						optionalIsNotPresent (
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
						stringFormat (
							"Route not configured correctly: %s",
							part.forwarderRoute.getCode ()));

				}

				// check route/number

				if (
					part.forwarderRoute != null

					&& part.forwarderRoute.getNumber ().length () > 0

					&& stringNotEqualSafe (
						part.forwarderRoute.getNumber (),
						part.numFrom)

				) {

					sendTemplateCheckError (
						work,
						part,
						SendError.invalidNumFrom,
						stringFormat (
							"Number is not valid for route: %s",
							part.numFrom));

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
							transaction,
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

					Optional <NetworkRec> networkOptional =
						networkHelper.find (
							transaction,
							part.networkId);

					if (
						optionalIsNotPresent (
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
							transaction,
							part.numTo);

					if (
						! smsTrackerManager.canSend (
							transaction,
							work.template.forwarder.getSmsTracker (),
							part.numToNumber,
							optionalAbsent ())
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
							transaction,
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
			@NonNull Transaction parentTransaction,
			@NonNull SendTemplateCheckWork work) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendTemplateCheckExisting");

		) {

			// check if any unqueueExMessages have already been sent

			for (
				SendPart sendPart
					: work.template.parts
			) {

				sendPart.forwarderMessageOut =
					findExistingForwarderMessageOut (
						transaction,
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

			// if some of these unqueueExMessages already exist but some don't then
			// stop

			if (work.sentSome && work.notSentSome) {

				work.template.errors.add (
					stringFormat (
						"Previously sent unqueueExMessages being resent in ",
						"different groups (very strange!)"));

				if (work.template.sendError == null) {

					work.template.sendError =
						SendError.reusedClientId;

				}

				work.ret = false;

			}

			// if all of them are previously sent, check the group is the same and
			// if so return the old result

			if (work.sentSome && !work.notSentSome) {

				for (int i = 0; i < work.template.parts.size() - 1; i++) {

					if (work.template.parts.get(i).forwarderMessageOut
							.getNextForwarderMessageOut() != work.template.parts
							.get(i + 1).forwarderMessageOut) {

						work.template.errors.add (
							stringFormat (
								"Previously sent unqueueExMessages being resent ",
								"in different groups (very strange!)"));

						if (work.template.sendError == null) {

							work.template.sendError =
								SendError.reusedClientId;

						}

						work.ret = false;

					}

				}

			}

		}

	}

	@Override
	public
	void sendTemplateSend (
			@NonNull Transaction parentTransaction,
			@NonNull SendTemplate sendTemplate) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendTemplateSend");

		) {

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
						transaction,
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

				if (threadId == null) {

					threadId =
						sendPart.forwarderMessageOut.getMessage ().getThreadId ();

				}

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

	}

	@Override
	public
	ForwarderMessageOutRec sendMessage (
			@NonNull Transaction parentTransaction,
			ForwarderRec forwarder,
			ForwarderMessageInRec fmIn,
			String message,
			String url,
			String numFrom,
			String numTo,
			String routeCode,
			String myId,
			Long pri,
			Collection <MediaRec> medias) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendMessage");

		) {

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

			if (
				! sendTemplateCheck (
					transaction,
					sendTemplate)
			) {

				if (
					enumEqualSafe (
						sendTemplate.sendError,
						SendError.trackerBlocked)
				) {

					return null;

				} else if (sendTemplate.errors.size () > 0) {

					throw new RuntimeException (
						sendTemplate.errors.get (0));

				} else {

					throw new RuntimeException (
						sendTemplate.parts.get (0).errors.get (0));

				}

			}

			sendTemplateSend (
				transaction,
				sendTemplate);

			return sendTemplate.parts.get (0).forwarderMessageOut;

		}

	}

	private
	ForwarderMessageOutRec findExistingForwarderMessageOut (
			@NonNull Transaction parentTransaction,
			ForwarderRec forwarder,
			ForwarderMessageInRec forwarderMessageIn,
			String messageText,
			String url,
			String numFrom,
			String numTo,
			ForwarderRouteRec route,
			String otherId,
			Long priority) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findExistingForwarderMessageOut");

		) {

			// if there is no client id, skip this check

			if (otherId == null)
				return null;

			// look up any existing message

			ForwarderMessageOutRec forwarderMessaeOut =
				forwarderMessageOutHelper.findByOtherId (
					transaction,
					forwarder,
					otherId);

			if (forwarderMessaeOut == null)
				return null;

			// make sure the message matches

			MessageRec message =
				forwarderMessaeOut.getMessage ();

			WapPushMessageRec wapPushMessage =
				ifThenElse (
					isNotNull (
						url),
					() -> wapPushMessageHelper.findRequired (
						transaction,
						message.getId ()),
					() -> null);

			if (

				optionalNotEqualOrNotPresentWithClass (
					ForwarderMessageInRec.class,
					optionalFromNullable (
						forwarderMessaeOut.getForwarderMessageIn ()),
					optionalFromNullable (
						forwarderMessageIn))

				|| stringNotEqualSafe (
					message.getNumFrom (),
					numFrom)

				|| stringNotEqualSafe (
					message.getNumTo (),
					numTo)

				|| referenceNotEqualWithClass (
					ForwarderRouteRec.class,
					forwarderMessaeOut.getForwarderRoute (),
					route)

				|| integerNotEqualSafe (
					message.getPri (),
					priority)

				|| (

					url == null

					&& (

						stringNotEqualSafe (
							message.getMessageType ().getCode (),
							"sms")

						|| stringNotEqualSafe (
							message.getText ().getText (),
							messageText)

					)

				) || (

					url != null

					&& (

						stringNotEqualSafe (
							message.getMessageType ().getCode (),
							"wap_push")

						|| stringNotEqualSafe (
							wapPushMessage.getTextText ().getText (),
							messageText)

						|| stringNotEqualSafe (
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

	}

	private
	ForwarderMessageOutRec createMessage (
			@NonNull Transaction parentTransaction,
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
			Set <String> tags,
			NetworkRec network,
			String subject,
			Collection <MediaRec> medias) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createMessage");

		) {

			// lookup some stuff

			NumberRec number =
				numberHelper.findOrCreate (
					transaction,
					numto);

			if (threadId == null && fmIn != null) {

				threadId =
					fmIn.getMessage ().getThreadId ();

			}

			// create the out record

			ForwarderMessageOutRec forwarderMessageOut =
				forwarderMessageOutHelper.insert (
					transaction,
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
					messageSenderProvider.provide (
						transaction)

					.threadId (
						threadId)

					.number (
						number)

					.messageString (
						transaction,
						message)

					.numFrom (
						numfrom)

					.routerResolve (
						transaction,
						forwarderRoute.getRouter ())

					.service (
						service)

					.deliveryTypeCode (
						transaction,
						"forwarder")

					.ref (
						forwarderMessageOut.getId ())

					.subjectString (
						transaction,
						optionalFromNullable (
							subject))

					.medias (
						medias)

					.sendNow (
						sendNow)

					.tags (
						tags)

					.network (
						network)

					.send (
						transaction);

			} else {

				messageOut =
					wapPushLogic.wapPushSend (
						transaction,
						threadId,
						number,
						numfrom,
						textHelper.findOrCreate (
							transaction,
							message),
						textHelper.findOrCreate (
							transaction,
							url),
						forwarderRoute.getRouter (),
						service,
						null,
						null,
						deliveryTypeHelper.findByCodeRequired (
							transaction,
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

}
