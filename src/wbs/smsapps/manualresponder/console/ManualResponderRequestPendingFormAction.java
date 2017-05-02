package wbs.smsapps.manualresponder.console;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThan;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.StringUtils.keyEqualsDecimalInteger;
import static wbs.utils.string.StringUtils.stringEqualSafe;

import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.console.action.ConsoleAction;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.gsm.GsmUtils;
import wbs.sms.keyword.logic.KeywordLogic;
import wbs.sms.message.outbox.logic.SmsMessageSender;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.logic.RouterLogic;
import wbs.sms.spendlimit.logic.SmsSpendLimitLogic;

import wbs.smsapps.manualresponder.logic.ManualResponderLogic;
import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderRec;
import wbs.smsapps.manualresponder.model.ManualResponderReplyObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderReplyRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateRec;

import wbs.web.responder.Responder;

@PrototypeComponent ("manualResponderRequestPendingFormAction")
public
class ManualResponderRequestPendingFormAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	ConsoleObjectManager consoleObjectManager;

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	KeywordLogic keywordLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ManualResponderLogic manualResponderLogic;

	@SingletonDependency
	ManualResponderReplyObjectHelper manualResponderReplyHelper;

	@SingletonDependency
	ManualResponderRequestObjectHelper manualResponderRequestHelper;

	@SingletonDependency
	ManualResponderTemplateObjectHelper manualResponderTemplateHelper;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	RouterLogic routerLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	SmsSpendLimitLogic smsSpendLimitLogic;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSenderProvider;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"manualResponderRequestPendingFormResponder");

	}

	// implementation

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"goReal");

		) {

			Long manualResponderRequestId =
				requestContext.stuffIntegerRequired (
					"manualResponderRequestId");

			String templateIdStr =
				requestContext.parameterRequired (
					"template-id");

			boolean ignore =
				stringEqualSafe (
					templateIdStr,
					"ignore");

			if (ignore) {

				return goIgnore (
					taskLogger,
					manualResponderRequestId);

			} else {

				return goSend (
					taskLogger,
					manualResponderRequestId,
					templateIdStr);

			}

		}

	}

	Responder goIgnore (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long manualResponderRequestId) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteFormat (
					logContext,
					parentTaskLogger,
					"goIgnore (%s)",
					keyEqualsDecimalInteger (
						"manualResponderRequestId",
						manualResponderRequestId));

		) {

			ManualResponderRequestRec manualResponderRequest =
				manualResponderRequestHelper.findRequired (
					transaction,
					manualResponderRequestId);

			// remove queue item

			queueLogic.processQueueItem (
				transaction,
				manualResponderRequest.getQueueItem (),
				userConsoleLogic.userRequired (
					transaction));

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

	}

	Responder goSend (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long manualResponderRequestId,
			@NonNull String templateIdString) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goSend");

		) {

			boolean sendAgain =
				false;

			// get params

			Long templateId =
				Long.parseLong (
					templateIdString);

			// get message

			String messageParam =
				optionalOrNull (
					requestContext.parameter (
						"message-" + templateId));

			// lookup objects

			ManualResponderRequestRec request =
				manualResponderRequestHelper.findRequired (
					transaction,
					manualResponderRequestId);

			ManualResponderNumberRec manualResponderNumber =
				request.getManualResponderNumber ();

			ManualResponderRec manualResponder =
				manualResponderNumber.getManualResponder ();

			ManualResponderTemplateRec template =
				manualResponderTemplateHelper.findRequired (
					transaction,
					templateId);

			// consistency checks

			if (
				referenceNotEqualWithClass (
					ManualResponderRec.class,
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

			if (! GsmUtils.gsmStringIsValid (messageString)) {

				requestContext.addError (
					"Message contains characters which cannot be sent via SMS");

				return null;

			}

			TextRec messageText =
				textHelper.findOrCreate (
					transaction,
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

			Pair <List <String>, Long> splitResult =
				manualResponderLogic.splitMessage (
					transaction,
					template,
					maxLengthPerMultipartMessage,
					messageString);

			List <String> messageParts =
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

				requestContext.addErrorFormat (
					"Message is too short at %s parts, ",
					integerToDecimalString (
						effectiveParts),
					"minimum is %s parts",
					integerToDecimalString (
						template.getMinimumMessageParts ()));

				return null;

			}

			// enforce maximum parts

			if (

				isNotNull (
					template.getMaximumMessages ())

				&& moreThan (
					effectiveParts,
					template.getMaximumMessages ())

			) {

				requestContext.addErrorFormat (
					"Message is too long at %s parts, ",
					integerToDecimalString (
						messageParts.size ()),
					"minimum is %s parts",
					integerToDecimalString (
						template.getMaximumMessages ()));

				return null;

			}

			// resolve route

			RouteRec route =
				routerLogic.resolveRouter (
					transaction,
					template.getRouter ());

			// check spend limit

			if (
				isNotNull (
					manualResponder.getSmsSpendLimiter ())
			) {

				Long amountToSpend =
					route.getOutCharge ()
					* messageParts.size ();

				Optional <Long> spendAvailable =
					smsSpendLimitLogic.spendCheck (
						transaction,
						manualResponder.getSmsSpendLimiter (),
						manualResponderNumber.getNumber ());

				if (
					optionalIsPresent (
						spendAvailable)
				) {

					if (
						equalToZero (
							spendAvailable.get ())
					) {

						requestContext.addErrorFormat (
							"User has reached their daily spend limit");

						return null;

					} else if (
						moreThan (
							amountToSpend,
							spendAvailable.get ())
					) {

						requestContext.addErrorFormat (
							"User has only %s left to spend, ",
							currencyLogic.formatText (
								manualResponder
									.getSmsSpendLimiter ()
									.getCurrency (),
								spendAvailable.get ()),
							"so can not spend %s",
							currencyLogic.formatText (
								manualResponder
									.getSmsSpendLimiter ()
									.getCurrency (),
								amountToSpend));

						return null;

					}

				}

			}

			// create reply

			ManualResponderReplyRec reply =
				manualResponderReplyHelper.insert (
					transaction,
					manualResponderReplyHelper.createInstance ()

				.setManualResponderRequest (
					request)

				.setUser (
					userConsoleLogic.userRequired (
						transaction))

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
						transaction,
						messagePart)

					.numFrom (
						template.getNumber ())

					.routerResolve (
						transaction,
						template.getRouter ())

					.serviceLookup (
						transaction,
						manualResponder,
						"default")

					.affiliate (
						optionalOrNull (
							manualResponderLogic.customerAffiliate (
								transaction,
								manualResponderNumber)))

					.user (
						userConsoleLogic.userRequired (
							transaction))

					.deliveryTypeCode (
						transaction,
						"manual_responder")

					.ref (
						reply.getId ())

					.sendNow (
						first
						|| ! template.getSequenceParts ())

					.send (
						transaction)

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

			// record spend

			if (
				isNotNull (
					manualResponder.getSmsSpendLimiter ())
			) {

				smsSpendLimitLogic.spend (
					transaction,
					manualResponder.getSmsSpendLimiter (),
					manualResponderNumber.getNumber (),
					reply.getMessages (),
					request.getMessage ().getThreadId (),
					request.getMessage ().getNumTo ());

			}

			// check if we can send again

			if (manualResponder.getCanSendMultiple ()) {

				sendAgain = true;

			} else {

				// process queue item

				queueLogic.processQueueItem (
					transaction,
					request.getQueueItem (),
					userConsoleLogic.userRequired (
						transaction));

				// update request

				request

					.setPending (
						false)

					.setUser (
						userConsoleLogic.userRequired (
							transaction))

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
						transaction,
						manualResponder,
						"default");

				keywordLogic.createOrUpdateKeywordSetFallback (
					transaction,
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
					"manualResponderRequestPendingFormResponder");

			} else {

				return responder (
					"queueHomeResponder");

			}

		}

	}

}