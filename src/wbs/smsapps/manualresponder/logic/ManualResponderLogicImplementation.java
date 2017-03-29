package wbs.smsapps.manualresponder.logic;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;

import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.customer.logic.SmsCustomerLogic;
import wbs.sms.gsm.GsmUtils;
import wbs.sms.gsm.MessageSplitter;
import wbs.sms.keyword.logic.KeywordLogic;
import wbs.sms.message.outbox.logic.SmsMessageSender;
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

	// singleton dependencies

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	KeywordLogic keywordLogic;

	@SingletonDependency
	ManualResponderReplyObjectHelper manualResponderReplyHelper;

	@SingletonDependency
	RouterLogic routerLogic;

	@SingletonDependency
	SmsCustomerLogic smsCustomerLogic;

	@SingletonDependency
	TextObjectHelper textHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSenderProvider;

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
				GsmUtils.gsmStringLength (
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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ManualResponderRequestRec request,
			@NonNull ManualResponderTemplateRec template) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendTemplateAutomatically");

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
				taskLogger,
				template.getDefaultText ());

		// resolve route

		RouteRec route =
			routerLogic.resolveRouter (
				template.getRouter ());

		// create reply

		ManualResponderReplyRec reply =
			manualResponderReplyHelper.insert (
				taskLogger,
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
					taskLogger,
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
						customerAffiliate (
							manualResponderNumber)))

				.deliveryTypeCode (
					"manual_responder")

				.ref (
					reply.getId ())

				.sendNow (
					first
					|| ! template.getSequenceParts ())

				.send (
					taskLogger)

			);

			first = false;

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
				taskLogger,
				template.getReplyKeywordSet (),
				request.getNumber (),
				command);

		}

	}

	@Override
	public
	Optional <AffiliateRec> customerAffiliate (
			@NonNull ManualResponderNumberRec number) {

		return ifThenElse (
			isNotNull (
				number.getSmsCustomer ()),

			() -> smsCustomerLogic.customerAffiliate (
				number.getSmsCustomer ()),

			() -> Optional.absent ()

		);

	}

}
