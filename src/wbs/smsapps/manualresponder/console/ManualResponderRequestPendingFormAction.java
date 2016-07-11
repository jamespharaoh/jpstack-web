package wbs.smsapps.manualresponder.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.lessThan;
import static wbs.framework.utils.etc.Misc.moreThan;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.optionalOrNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import org.apache.commons.lang3.tuple.Pair;

import wbs.console.action.ConsoleAction;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.gsm.Gsm;
import wbs.sms.keyword.logic.KeywordLogic;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.logic.RouterLogic;
import wbs.smsapps.manualresponder.logic.ManualResponderLogic;
import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderRec;
import wbs.smsapps.manualresponder.model.ManualResponderReplyObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderReplyRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateRec;

@PrototypeComponent ("manualResponderRequestPendingFormAction")
public
class ManualResponderRequestPendingFormAction
	extends ConsoleAction {

	// dependencies

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	ConsoleObjectManager consoleObjectManager;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	KeywordLogic keywordLogic;

	@Inject
	ManualResponderLogic manualResponderLogic;

	@Inject
	ManualResponderReplyObjectHelper manualResponderReplyHelper;

	@Inject
	ManualResponderRequestObjectHelper manualResponderRequestHelper;

	@Inject
	ManualResponderTemplateObjectHelper manualResponderTemplateHelper;

	@Inject
	QueueLogic queueLogic;

	@Inject
	RouterLogic routerLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@Inject
	Provider<MessageSender> messageSender;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			"manualResponderRequestPendingFormResponder");

	}

	// implementation

	@Override
	public
	Responder goReal () {

		int manualResponderRequestId =
			requestContext.stuffInt (
				"manualResponderRequestId");

		String templateIdStr =
			requestContext.parameterRequired (
				"template-id");

		boolean ignore =
			equal (
				templateIdStr,
				"ignore");

		if (ignore) {

			return goIgnore (
				manualResponderRequestId);

		} else {

			return goSend (
				manualResponderRequestId,
				templateIdStr);

		}

	}

	Responder goIgnore (
			int manualResponderRequestId) {

		// start transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ManualResponderRequestRec manualResponderRequest =
			manualResponderRequestHelper.findRequired (
				manualResponderRequestId);

		// remove queue item

		queueLogic.processQueueItem (
			manualResponderRequest.getQueueItem (),
			userConsoleLogic.userRequired ());

		// mark request as not pending

		manualResponderRequest

			.setPending (
				false);

		// done

		transaction.commit ();

		requestContext.addNotice (
			"Request ignored");

		return responder (
			"queueHomeResponder");

	}

	Responder goSend (
			int manualResponderRequestId,
			String templateIdString) {

		boolean sendAgain =
			false;

		// get params

		int templateId =
			Integer.parseInt (
				templateIdString);

		// get message

		String messageParam =
			optionalOrNull (
				requestContext.parameter (
					"message-" + templateId));

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ManualResponderRequestRec request =
			manualResponderRequestHelper.findRequired (
				manualResponderRequestId);

		ManualResponderNumberRec manualResponderNumber =
			request.getManualResponderNumber ();

		ManualResponderRec manualResponder =
			manualResponderNumber.getManualResponder ();

		ManualResponderTemplateRec template =
			manualResponderTemplateHelper.findRequired (
				templateId);

		// consistency checks

		if (
			notEqual (
				template.getManualResponder (),
				manualResponder)
		) {

			requestContext.addError (
				"Internal error");

			return null;

		}

		boolean customisable =
			template.getCustomisable ();

		if (
			(customisable && messageParam == null)
			|| (! customisable && messageParam != null)
		) {

			requestContext.addError (
				"Internal error");

			return null;

		}

		// work out message text

		String messageString =
			template.getCustomisable ()
				? messageParam
				: template.getDefaultText ();

		if (! Gsm.isGsm (messageString)) {

			requestContext.addError (
				"Message contains characters which cannot be sent via SMS");

			return null;

		}

		TextRec messageText =
			textHelper.findOrCreate (
				messageString);

		boolean shortMessageParts =
			request
				.getNumber ()
				.getNetwork ()
				.getShortMultipartMessages ();

		long maxLengthPerMultipartMessage =
			shortMessageParts
				? 134l
				: 153l;

		Pair<List<String>,Long> splitResult =
			manualResponderLogic.splitMessage (
				template,
				maxLengthPerMultipartMessage,
				messageString);

		List<String> messageParts =
			splitResult.getLeft ();

		long effectiveParts =
			splitResult.getRight ();

		// enforce minimum parts

		if (

			template.getMinimumMessageParts () != null

			&& lessThan (
				effectiveParts,
				template.getMinimumMessageParts ())

		) {

			requestContext.addError (
				stringFormat (
					"Message is too short at %s parts, ",
					effectiveParts,
					"minimum is %s parts",
					template.getMinimumMessageParts ()));

			return null;

		}

		// enforce maximum parts

		if (

			template.getMaximumMessages () != null

			&& moreThan (
				effectiveParts,
				template.getMaximumMessages ())

		) {

			requestContext.addError (
				stringFormat (
					"Message is too long at %s parts, ",
					messageParts.size (),
					"minimum is %s parts",
					template.getMaximumMessages ()));

			return null;

		}

		// resolve route

		RouteRec route =
			routerLogic.resolveRouter (
				template.getRouter ());

		// create reply

		ManualResponderReplyRec reply =
			manualResponderReplyHelper.insert (
				manualResponderReplyHelper.createInstance ()

			.setManualResponderRequest (
				request)

			.setUser (
				userConsoleLogic.userRequired ())

			.setText (
				messageText)

			.setTimestamp (
				transaction.now ())

			.setNumFreeMessages (
				route.getOutCharge () == 0
					? effectiveParts
					: 0l)

			.setNumBilledMessages (
				route.getOutCharge () > 0
					? effectiveParts
					: 0l)

		);

		// send messages

		boolean first = true;

		for (
			String messagePart
				: messageParts
		) {

			reply.getMessages ().add (
				messageSender.get ()

				.threadId (
					request.getMessage ().getThreadId ())

				.number (
					request.getNumber ())

				.messageString (
					messagePart)

				.numFrom (
					template.getNumber ())

				.routerResolve (
					template.getRouter ())

				.serviceLookup (
					manualResponder,
					"default")

				.affiliate (
					optionalOrNull (
						manualResponderLogic.customerAffiliate (
							manualResponderNumber)))

				.user (
					userConsoleLogic.userRequired ())

				.deliveryTypeCode (
					"manual_responder")

				.ref (
					(long)
					reply.getId ())

				.sendNow (
					first
					|| ! template.getSequenceParts ())

				.send ()

			);

			first = false;

		}

		// update request

		request

			.setNumFreeMessages (
				+ request.getNumFreeMessages ()
				+ route.getOutCharge () == 0
					? effectiveParts
					: 0l)

			.setNumBilledMessages (
				+ request.getNumBilledMessages ()
				+ route.getOutCharge () > 0
					? effectiveParts
					: 0l);

		// check if we can send again

		if (manualResponder.getCanSendMultiple ()) {

			sendAgain = true;

		} else {

			// process queue item

			queueLogic.processQueueItem (
				request.getQueueItem (),
				userConsoleLogic.userRequired ());

			// update request

			request

				.setPending (
					false)

				.setUser (
					userConsoleLogic.userRequired ())

				.setProcessedTime (
					transaction.now ());

		}

		// create keyword set fallback if appropriate

		if (
			isNotNull (
				template.getReplyKeywordSet ())
		) {

			CommandRec command =
				commandHelper.findByCodeRequired (
					manualResponder,
					"default");

			keywordLogic.createOrUpdateKeywordSetFallback (
				template.getReplyKeywordSet (),
				request.getNumber (),
				command);

		}

		// done

		transaction.commit ();

		requestContext.addNotice (
			"Reply sent");

		// choose appropriate responder

		if (sendAgain) {

			return responder (
				"manualResponderPendingFormResponder");

		} else {

			return responder (
				"queueHomeResponder");

		}

	}

}
