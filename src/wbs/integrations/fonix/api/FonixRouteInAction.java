package wbs.integrations.fonix.api;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.api.mvc.ApiLoggingAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromGeneric;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.fonix.logic.FonixLogic;
import wbs.integrations.fonix.model.FonixInboundLogObjectHelper;
import wbs.integrations.fonix.model.FonixInboundLogType;
import wbs.integrations.fonix.model.FonixRouteInObjectHelper;
import wbs.integrations.fonix.model.FonixRouteInRec;

import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.web.TextResponder;

import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.utils.string.FormatWriter;

import wbs.web.context.RequestContext;
import wbs.web.exceptions.HttpNotFoundException;
import wbs.web.responder.Responder;

@PrototypeComponent ("fonixRouteInAction")
public
class FonixRouteInAction
	extends ApiLoggingAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	FonixInboundLogObjectHelper fonixInboundLogHelper;

	@SingletonDependency
	FonixLogic fonixLogic;

	@SingletonDependency
	FonixRouteInObjectHelper fonixRouteInHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	NumberObjectHelper smsNumberHelper;

	@SingletonDependency
	RouteObjectHelper smsRouteHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <TextResponder> textResponderProvider;

	// state

	FonixRouteInRequest request;
	Boolean success = false;

	// implementation

	@Override
	protected
	void processRequest (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter debugWriter) {

		// decode request

		request =
			new DataFromGeneric ()

			.fromMap (
				FonixRouteInRequest.class,
				requestContext.parameterMapSimple ());

	}

	@Override
	protected
	void updateDatabase (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"updateDatabase");

		// begin transaction

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					stringFormat (
						"%s.%s ()",
						getClass ().getSimpleName (),
						"updateDatabase"),
					this);

		) {

			// lookup route

			Optional <RouteRec> smsRouteOptional =
				smsRouteHelper.find (
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"smsRouteId")));

			if (

				optionalIsNotPresent (
					smsRouteOptional)

			) {

				throw new HttpNotFoundException (
					optionalOf (
						stringFormat (
							"Route %s does not exist",
							requestContext.requestStringRequired (
								"smsRouteId"))),
					emptyList ());

			}

			if (smsRouteOptional.get ().getDeleted ()) {

				throw new HttpNotFoundException (
					optionalOf (
						stringFormat (
							"Route %s.%s has been deleted",
							smsRouteOptional.get ().getSlice ().getCode (),
							smsRouteOptional.get ().getCode ())),
					emptyList ());

			}

			if (! smsRouteOptional.get ().getCanReceive ()) {

				throw new HttpNotFoundException (
					optionalOf (
						stringFormat (
							"Route %s.%s is not configured for inbound messages",
							smsRouteOptional.get ().getSlice ().getCode (),
							smsRouteOptional.get ().getCode ())),
					emptyList ());

			}

			RouteRec smsRoute =
				optionalGetRequired (
					smsRouteOptional);

			// lookup fonix route in

			Optional <FonixRouteInRec> fonixRouteInOptional =
				fonixRouteInHelper.find (
					smsRoute.getId ());

			if (

				optionalIsNotPresent (
					fonixRouteInOptional)

				|| fonixRouteInOptional.get ().getDeleted ()

			) {

				throw new HttpNotFoundException (
					optionalAbsent (),
					emptyList ());

			}

			@SuppressWarnings ("unused")
			FonixRouteInRec fonixRouteIn =
				optionalGetRequired (
					fonixRouteInOptional);

			// insert message

			smsInboxLogic.inboxInsert (
				taskLogger,
				optionalOf (
					request.guid ()),
				textHelper.findOrCreate (
					taskLogger,
					request.body ()),
				smsNumberHelper.findOrCreate (
					taskLogger,
					request.moNumber ()),
				request.destination (),
				smsRoute,
				optionalAbsent (),
				optionalOf (
					fonixLogic.stringToInstant (
						request.receiveTime ())),
				emptyList (),
				optionalAbsent (),
				optionalAbsent ());

			// commit and return

			transaction.commit ();

		}

		success = true;

	}

	@Override
	protected
	Responder createResponse (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter debugWriter) {

		return textResponderProvider.get ()

			.text (
				"OK");

	}

	@Override
	protected
	void storeLog (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String debugLog) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"storeLog");

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"ClockworkSmsRouteInAction.storeLog ()",
					this);

		) {

			fonixInboundLogHelper.insert (
				taskLogger,
				fonixInboundLogHelper.createInstance ()

				.setRoute (
					smsRouteHelper.findRequired (
						Long.parseLong (
							requestContext.requestStringRequired (
								"smsRouteId"))))

				.setType (
					FonixInboundLogType.smsMessage)

				.setTimestamp (
					transaction.now ())

				.setDetails (
					debugLog)

				.setSuccess (
					success)

			);

			transaction.commit ();

		}

	}

}
