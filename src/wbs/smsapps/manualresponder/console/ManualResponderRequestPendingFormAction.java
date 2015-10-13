package wbs.smsapps.manualresponder.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
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
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.gsm.Gsm;
import wbs.sms.gsm.MessageSplitter;
import wbs.sms.keyword.logic.KeywordLogic;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.smsapps.manualresponder.logic.ManualResponderLogic;
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
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	UserObjectHelper userHelper;

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
			requestContext.parameter (
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
			manualResponderRequestHelper.find (
				manualResponderRequestId);

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		// remove queue item

		queueLogic.processQueueItem (
			manualResponderRequest.getQueueItem (),
			myUser);

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
			requestContext.parameter (
				"message-" + templateId);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ManualResponderRequestRec request =
			manualResponderRequestHelper.find (
				manualResponderRequestId);

		ManualResponderRec manualResponder =
			request.getManualResponder ();

		ManualResponderTemplateRec template =
			manualResponderTemplateHelper.find (
				templateId);

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

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

		// split message and apply templates

		List<String> messageParts;

		if (
			template.getSplitLong ()
		) {

			// split message

			MessageSplitter.Templates messageSplitterTemplates;

			if (
				template.getApplyTemplates ()
			) {

				// use configured templates

				messageSplitterTemplates =
					new MessageSplitter.Templates (
						template.getSingleTemplate (),
						template.getFirstTemplate (),
						template.getMiddleTemplate (),
						template.getLastTemplate ());

			} else {

				// just split message

				messageSplitterTemplates =
					new MessageSplitter.Templates (
						"{message}",
						"{message}",
						"{message}",
						"{message}");

			}

			messageParts =
				MessageSplitter.split (
					messageString,
					messageSplitterTemplates);

			// enforce minimum and maximum message counts

			if (

				messageParts.size ()
					> template.getMaximumMessages ()

				|| messageParts.size ()
					< template.getMinimumMessageParts ()

			) {

				requestContext.addError (
					stringFormat (
						"Unable to split message into %s parts",
						template.getMaximumMessages ()));

				return null;

			}

		} else {

			// don't split message, apply template if enabled

			if (template.getApplyTemplates ()) {

				messageParts =
					Collections.singletonList (
						template.getSingleTemplate ().replaceAll (
							Pattern.quote ("{message}"),
							messageString));

			} else {

				messageParts =
					Collections.singletonList (
						messageString);

			}

			// enforce max and min length

			if (messageParam != null) {

				int maxLength =
					manualResponderLogic.maximumMessageLength (
						request,
						template);

				int minLength =
					manualResponderLogic.minimumMessageLength (
						request,
						template);

				int actualLength =
					Gsm.length (
						messageParam);

				if (actualLength > maxLength) {

					requestContext.addError (
						stringFormat (
							"Message is too long, contains %s chars, ",
							actualLength,
							"maximum is %s",
							maxLength));

					return null;

				}

				if (actualLength < minLength) {

					requestContext.addError (
						stringFormat (
							"Message is too short, contains %s chars, ",
							actualLength,
							"minimum is %s",
							minLength));

					return null;

				}

			}

		}

		// create reply

		ManualResponderReplyRec reply =
			manualResponderReplyHelper.insert (
				new ManualResponderReplyRec ()

			.setManualResponderRequest (
				request)

			.setUser (
				myUser)

			.setText (
				messageText)

			.setTimestamp (
				transaction.now ().toDate ())

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

				.user (
					myUser)

				.deliveryTypeCode (
					"manual_responder")

				.ref (
					reply.getId ())

				.sendNow (
					first
					|| ! template.getSequenceParts ())

				.send ()

			);

			first = false;

		}

		if (manualResponder.getCanSendMultiple ()) {

			sendAgain = true;

		} else {

			// process queue item

			queueLogic.processQueueItem (
				request.getQueueItem (),
				myUser);

			// update request

			request

				.setPending (
					false);

		}

		// create keyword set fallback if appropriate

		if (
			isNotNull (
				template.getReplyKeywordSet ())
		) {

			CommandRec command =
				commandHelper.findByCode (
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
