package wbs.integrations.oxygen8.api;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.BinaryUtils.bytesFromHex;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.bytesToString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.secondsToInstant;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.integrations.oxygen8.model.Oxygen8ConfigRec;
import wbs.integrations.oxygen8.model.Oxygen8InboundLogObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8InboundLogType;
import wbs.integrations.oxygen8.model.Oxygen8NetworkObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8NetworkRec;
import wbs.integrations.oxygen8.model.Oxygen8RouteInObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8RouteInRec;

import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.web.TextResponder;

import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

@PrototypeComponent ("oxygen8RouteInSmsAction")
public
class Oxygen8RouteInSmsAction
	extends ApiAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionUtils exceptionLogic;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	Oxygen8InboundLogObjectHelper oxygen8InboundLogHelper;

	@SingletonDependency
	Oxygen8NetworkObjectHelper oxygen8NetworkHelper;

	@SingletonDependency
	Oxygen8RouteInObjectHelper oxygen8RouteInHelper;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	NumberObjectHelper smsNumberHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <TextResponder> textResponderProvider;

	// state

	StringBuilder debugLog =
		new StringBuilder ();

	Long routeId;

	String channel;
	String reference;
	String trigger;
	String shortcode;
	String msisdn;
	String rawContent;
	String textContent;
	Integer dataType;
	Long dateReceived;
	Integer campaignId;

	Boolean success = false;

	// implementation

	@Override
	protected
	Responder goApi (
			@NonNull TaskLogger parentTaskLogger) {

		try {

			logRequest ();

			processRequest ();

			updateDatabase ();

			return createResponse ();

		} catch (RuntimeException exception) {

			logFailure (
				exception);

			throw exception;

		} finally {

			storeLog ();

		}

	}

	void logRequest () {

		// output

		debugLog.append (
			stringFormat (
				"%s %s\n",
				requestContext.method (),
				requestContext.requestUri ()));

		// output headers

		for (
			Map.Entry <String, List <String>> headerEntry
				: requestContext.headerMap ().entrySet ()
		) {

			for (
				String headerValue
					: headerEntry.getValue ()
			) {

				debugLog.append (
					stringFormat (
						"%s = %s\n",
						headerEntry.getKey (),
						headerValue));

			}

		}

		debugLog.append (
			stringFormat (
				"\n"));

		// output params

		for (
			Map.Entry <String, List <String>> parameterEntry
				: requestContext.parameterMap ().entrySet ()
		) {

			for (
				String parameterValue
					: parameterEntry.getValue ()
			) {

				debugLog.append (
					stringFormat (
						"%s = %s\n",
						parameterEntry.getKey (),
						parameterValue));

			}

		}

		debugLog.append (
			stringFormat (
				"\n"));

	}

	void logFailure (
			Throwable exception) {

		debugLog.append (
			stringFormat (
				"*** THREW EXCEPTION ***\n",
				"\n"));

		debugLog.append (
			stringFormat (
				"%s\n",
				exceptionLogic.throwableDump (
					exception)));

	}

	void storeLog () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"Oxygen8InboundSmsAction.storeLog ()",
				this);

		oxygen8InboundLogHelper.insert (
			oxygen8InboundLogHelper.createInstance ()

			.setRoute (
				routeHelper.findRequired (
					requestContext.requestIntegerRequired (
						"routeId")))

			.setType (
				Oxygen8InboundLogType.smsMessage)

			.setTimestamp (
				transaction.now ())

			.setDetails (
				debugLog.toString ())

			.setSuccess (
				success)

		);

		transaction.commit ();

		success = true;

	}

	void processRequest () {

		routeId =
			requestContext.requestIntegerRequired (
				"routeId");

		channel =
			requestContext.parameterOrNull (
				"Channel");

		reference =
			requestContext.parameterOrNull (
				"Reference");

		trigger =
			requestContext.parameterOrNull (
				"Trigger");

		shortcode =
			requestContext.parameterOrNull (
				"Shortcode");

		msisdn =
			requestContext.parameterOrNull (
				"MSISDN");

		rawContent =
			requestContext.parameterOrNull (
				"Content");

		dataType =
			Integer.parseInt (
				requestContext.parameterOrNull (
					"DataType"));

		switch (dataType) {

		case 0:

			textContent = rawContent;

			break;

		case 3:

			textContent =
				bytesToString (
					bytesFromHex (
						rawContent),
					"utf-16be");

			break;

		default:

			throw new RuntimeException (
				stringFormat (
					"Don't know how to handle data type %s",
					integerToDecimalString (
						dataType)));

		}

		dateReceived =
			Long.parseLong (
				requestContext.parameterOrNull ("DateReceived"));

		campaignId =
			Integer.parseInt (
				requestContext.parameterOrNull ("CampaignID"));

	}

	void updateDatabase () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"Oxygen8InboundSmsAction.updateDatabase ()",
				this);

		RouteRec route =
			routeHelper.findRequired (
				routeId);

		Oxygen8RouteInRec oxygen8RouteIn =
			oxygen8RouteInHelper.findRequired (
				route.getId ());

		Oxygen8ConfigRec oxygen8Config =
			oxygen8RouteIn.getOxygen8Config ();

		Oxygen8NetworkRec oxygen8Network =
			oxygen8NetworkHelper.findByChannel (
				oxygen8Config,
				channel);

		if (oxygen8Network == null) {

			throw new RuntimeException (
				stringFormat (
					"Oxygen8 channel not recognised: %s",
					channel));

		}

		smsInboxLogic.inboxInsert (
			optionalOf (
				reference),
			textHelper.findOrCreate (
				textContent),
			smsNumberHelper.findOrCreate (
				msisdn),
			shortcode,
			route,
			optionalOf (
				oxygen8Network.getNetwork ()),
			optionalOf (
				secondsToInstant (
					dateReceived)),
			emptyList (),
			optionalAbsent (),
			optionalAbsent ());

		transaction.commit ();

	}

	Responder createResponse () {

		return textResponderProvider.get ()
			.text ("success");

	}

}
