package wbs.smsapps.manualresponder.logic;

import static wbs.framework.utils.etc.Misc.isNotNull;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.gsm.Gsm;
import wbs.sms.gsm.MessageSplitter;
import wbs.sms.keyword.logic.KeywordLogic;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.logic.RouterLogic;
import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderRec;
import wbs.smsapps.manualresponder.model.ManualResponderReplyObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderReplyRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateRec;

@SingletonComponent ("manualResponderLogic")
public
class ManualResponderLogicImplementation
	implements ManualResponderLogic {

	// dependencies

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	KeywordLogic keywordLogic;

	@Inject
	ManualResponderReplyObjectHelper manualResponderReplyHelper;

	@Inject
	RouterLogic routerLogic;

	@Inject
	TextObjectHelper textHelper;

	// prototype dependencies

	@Inject
	Provider<MessageSender> messageSenderProvider;

	// implementation

	@Override
	public
	Pair<List<String>,Long> splitMessage (
			@NonNull ManualResponderTemplateRec template,
			@NonNull Long maxLengthPerMultipartMessage,
			@NonNull String messageString) {

		// split message and apply templates

		List<String> messageParts;
		long effectiveParts;

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

			effectiveParts =
				messageParts.size ();

		} else {

			// don't split message, apply template if enabled

			String singleMessage;

			if (template.getApplyTemplates ()) {

				singleMessage =
					template.getSingleTemplate ().replaceAll (
						Pattern.quote ("{message}"),
						messageString);

			} else {

				singleMessage =
					messageString;

			}

			long singleMessageLength =
				Gsm.length (
					singleMessage);

			messageParts =
				Collections.singletonList (
					singleMessage);

			// work out effective parts

			if (singleMessageLength <= 160l) {

				effectiveParts = 1l;

			} else {

				effectiveParts = (
					singleMessageLength - 1l
				) / maxLengthPerMultipartMessage + 1l;

			}

		}

		// return

		return Pair.of (
			messageParts,
			effectiveParts);

	}

	@Override
	public
	void sendTemplateAutomatically (
			@NonNull ManualResponderRequestRec request,
			@NonNull ManualResponderTemplateRec template) {

		Transaction transaction =
			database.currentTransaction ();

		ManualResponderNumberRec manualResponderNumber =
			request.getManualResponderNumber ();

		ManualResponderRec manualResponder =
			manualResponderNumber.getManualResponder ();

		// split message

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
			splitMessage (
				template,
				maxLengthPerMultipartMessage,
				template.getDefaultText ());

		List<String> messageParts =
			splitResult.getLeft ();

		Long effectiveParts =
			splitResult.getRight ();

		TextRec messageText =
			textHelper.findOrCreate (
				template.getDefaultText ());

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
				messageSenderProvider.get ()

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

	}

}
