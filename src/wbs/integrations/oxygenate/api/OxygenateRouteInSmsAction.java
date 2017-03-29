package wbs.integrations.oxygenate.api;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.BinaryUtils.bytesFromHex;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.bytesToString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.secondsToInstant;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.integrations.oxygenate.model.OxygenateConfigRec;
import wbs.integrations.oxygenate.model.OxygenateInboundLogObjectHelper;
import wbs.integrations.oxygenate.model.OxygenateInboundLogType;
import wbs.integrations.oxygenate.model.OxygenateNetworkObjectHelper;
import wbs.integrations.oxygenate.model.OxygenateNetworkRec;
import wbs.integrations.oxygenate.model.OxygenateRouteInObjectHelper;
import wbs.integrations.oxygenate.model.OxygenateRouteInRec;

import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.web.TextResponder;

import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

@PrototypeComponent ("oxygenateRouteInSmsAction")
public
class OxygenateRouteInSmsAction
	extends ApiAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionUtils exceptionLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	OxygenateInboundLogObjectHelper oxygenateInboundLogHelper;

	@SingletonDependency
	OxygenateNetworkObjectHelper oxygenateNetworkHelper;

	@SingletonDependency
	OxygenateRouteInObjectHelper oxygenateRouteInHelper;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

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

	Long smsRouteId;

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

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goApi");

		try {

			logRequest ();

			processRequest ();

			updateDatabase (
				taskLogger);

			return createResponse ();

		} catch (RuntimeException exception) {

			logFailure (
				exception);

			throw exception;

		} finally {

			storeLog (
				taskLogger);

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

	void storeLog (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"storeLog");

		try (

			Transaction transaction =
				database.beginReadWrite (
					"Oxygen8InboundSmsAction.storeLog ()",
					this);

		) {

			oxygenateInboundLogHelper.insert (
				taskLogger,
				oxygenateInboundLogHelper.createInstance ()

				.setRoute (
					routeHelper.findRequired (
						parseIntegerRequired (
							requestContext.requestStringRequired (
								"smsRouteId"))))

				.setType (
					OxygenateInboundLogType.smsMessage)

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

	}

	void processRequest () {

		smsRouteId =
			parseIntegerRequired (
				requestContext.requestStringRequired (
					"smsRouteId"));

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
				requestContext.parameterOrNull (
					"DateReceived"));

		campaignId =
			Integer.parseInt (
				requestContext.parameterOrNull (
					"CampaignID"));

	}

	void updateDatabase (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"updateDatabase");

		try (

			Transaction transaction =
				database.beginReadWrite (
					"Oxygen8InboundSmsAction.updateDatabase ()",
					this);

		) {

			RouteRec route =
				routeHelper.findRequired (
					smsRouteId);

			OxygenateRouteInRec oxygenateRouteIn =
				oxygenateRouteInHelper.findRequired (
					route.getId ());

			OxygenateConfigRec oxygenateConfig =
				oxygenateRouteIn.getOxygenateConfig ();

			Optional <OxygenateNetworkRec> oxygenateNetworkOptional =
				oxygenateNetworkHelper.findByChannel (
					oxygenateConfig,
					channel);

			if (
				optionalIsNotPresent (
					oxygenateNetworkOptional)
			) {

				throw new RuntimeException (
					stringFormat (
						"Oxygen8 channel not recognised: %s",
						channel));

			}

			OxygenateNetworkRec oxygenateNetwork =
				oxygenateNetworkOptional.get ();

			smsInboxLogic.inboxInsert (
				taskLogger,
				optionalOf (
					reference),
				textHelper.findOrCreate (
					taskLogger,
					textContent),
				smsNumberHelper.findOrCreate (
					taskLogger,
					msisdn),
				shortcode,
				route,
				optionalOf (
					oxygenateNetwork.getNetwork ()),
				optionalOf (
					secondsToInstant (
						dateReceived)),
				emptyList (),
				optionalAbsent (),
				optionalAbsent ());

			transaction.commit ();

		}

	}

	Responder createResponse () {

		return textResponderProvider.get ()
			.text ("success");

	}

}
