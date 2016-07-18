package wbs.test.simulator.console;

import static wbs.framework.utils.etc.Misc.doesNotStartWith;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifElse;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.toBoolean;
import static wbs.framework.utils.etc.Misc.toInteger;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import org.joda.time.Instant;
import org.json.simple.JSONValue;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.object.ObjectManager;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.Responder;
import wbs.platform.media.model.MediaRec;
import wbs.platform.scaffold.console.RootConsoleHelper;
import wbs.platform.scaffold.console.SliceConsoleHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.report.logic.ReportLogic;
import wbs.sms.network.console.NetworkConsoleHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.console.NumberConsoleHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.test.simulator.model.SimulatorEventObjectHelper;
import wbs.test.simulator.model.SimulatorRec;
import wbs.test.simulator.model.SimulatorRouteRec;
import wbs.test.simulator.model.SimulatorSessionNumberObjectHelper;
import wbs.test.simulator.model.SimulatorSessionNumberRec;
import wbs.test.simulator.model.SimulatorSessionObjectHelper;
import wbs.test.simulator.model.SimulatorSessionRec;

@PrototypeComponent ("simulatorSessionCreateEventAction")
public
class SimulatorSessionCreateEventAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	NetworkConsoleHelper networkHelper;

	@Inject
	NumberConsoleHelper numberHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	ReportLogic reportLogic;

	@Inject
	RootConsoleHelper rootHelper;

	@Inject
	RouteConsoleHelper routeHelper;

	@Inject
	SimulatorEventObjectHelper simulatorEventHelper;

	@Inject
	SimulatorSessionObjectHelper simulatorSessionHelper;

	@Inject
	SimulatorSessionNumberObjectHelper simulatorSessionNumberHelper;

	@Inject
	SliceConsoleHelper sliceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@Inject
	Provider<JsonResponder> jsonResponder;

	// details

	@Override
	protected
	Responder backupResponder () {
		return null;
	}

	@Override
	protected
	Responder goReal () {

		try {

			String type =
				requestContext.getForm ("type");

			if (equal (type, "sendMessage"))
				return sendMessage ();

			if (equal (type, "deliveryReport"))
				return deliveryReport ();

			throw new AjaxException (
				stringFormat (
					"Invalid event type: %s",
					type));

		} catch (AjaxException error) {

			return jsonResponder.get ()

				.value (
					ImmutableMap.<Object,Object>builder ()

					.put (
						"success",
						false)

					.put (
						"error",
						error.message)

					.build ()

				);

		} catch (RuntimeException exception) {

			exceptionLogger.logThrowable (
				"console",
				requestContext.requestPath (),
				exception,
				Optional.of (
					userConsoleLogic.userIdRequired ()),
				GenericExceptionResolution.ignoreWithUserWarning);

			return jsonResponder.get ()

				.value (
					ImmutableMap.<Object,Object>builder ()

					.put (
						"success",
						false)

					.put (
						"error",
						"internal error")

					.build ()

				);

		}

	}

	Responder sendMessage () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"SimulatorSessionCreateEventAction.sendMessage ()",
				this);

		SimulatorSessionRec simulatorSession =
			simulatorSessionHelper.findRequired (
				requestContext.stuffInt (
					"simulatorSessionId"));

		SimulatorRec simulator =
			simulatorSession.getSimulator ();

		String numFrom =
			requestContext.getForm (
				"numFrom");

		String numTo =
			requestContext.getForm (
				"numTo");

		String messageText =
			requestContext.getForm (
				"message");

		NetworkRec network =
			networkHelper.findRequired (
				toInteger (
					requestContext.getForm (
						"networkId")));

		// store in session

		requestContext.session (
			"simulatorNumFrom",
			numFrom);

		requestContext.session (
			"simulatorNumTo",
			numTo);

		requestContext.session (
			"simulatorMessage",
			messageText);

		requestContext.session (
			"simulatorNetworkId",
			network.getId ().toString ());

		// work out route

		Optional<RouteRec> routeOption =
			resolveRoute (
				simulator,
				numTo);

		if (! routeOption.isPresent ()) {

			throw new AjaxException (
				"No route configured for that number");

		}

		RouteRec route =
			routeOption.get ();

		// insert inbox

		MessageRec message =
			inboxLogic.inboxInsert (
				Optional.<String>absent (),
				textHelper.findOrCreate (messageText),
				numFrom,
				numTo,
				route,
				Optional.of (network),
				Optional.<Instant>absent (),
				Collections.<MediaRec>emptyList (),
				Optional.<String>absent (),
				Optional.<String>absent ());

		// create event data

		Object data =
			ImmutableMap.<String,Object>builder ()
				.put ("message", ImmutableMap.<String,Object>builder ()
					.put ("id", message.getId ())
					.put ("numFrom", numFrom)
					.put ("numTo", numTo)
					.put ("text", messageText)
					.build ())
				.put ("route", ImmutableMap.<String,Object>builder ()
					.put ("id", route.getId ())
					.put ("code", route.getCode ())
					.build ())
				.put ("network", ImmutableMap.<String,Object>builder ()
					.put ("id", network.getId ())
					.put ("code", network.getCode ())
					.build ())
				.build ();

		// create event

		simulatorEventHelper.insert (
			simulatorEventHelper.createInstance ()

			.setSimulatorSession (
				simulatorSession)

			.setType (
				"message_in")

			.setTimestamp (
				transaction.now ())

			.setData (
				JSONValue.toJSONString (data))

		);

		// associate number with session

		NumberRec number =
			numberHelper.findOrCreate (
				numFrom);

		SimulatorSessionNumberRec simulatorSessionNumber =
			simulatorSessionNumberHelper.findOrCreate (
				number);

		simulatorSessionNumber

			.setSimulatorSession (
				simulatorSession);

		// done

		transaction.commit ();

		return jsonResponder.get ()
			.value (
				ImmutableMap.<Object,Object>builder ()
					.put ("success", true)
					.build ());

	}

	Responder deliveryReport () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"SimulatorSessionCreateEventAction.deliveryReport ()",
				this);

		SimulatorSessionRec simulatorSession =
			simulatorSessionHelper.findRequired (
				requestContext.stuffInt (
					"simulatorSessionId"));

		Integer messageId =
			toInteger (
				requestContext.getForm (
					"messageId"));

		Boolean success =
			toBoolean (
				requestContext.getForm (
					"success"));

		// submit delivery report

		reportLogic.deliveryReport (
			messageId,
			ifElse (
				success,
				() -> MessageStatus.delivered,
				() -> MessageStatus.undelivered),
			transaction.now (),
			null);

		// create event data

		Object data =
			ImmutableMap.<String,Object>builder ()

			.put (
				"deliveryReport",
				ImmutableMap.<String,Object>builder ()

				.put (
					"messageId",
					messageId)

				.put (
					"success",
					success)

				.build ()

			)

			.build ();

		// create event

		simulatorEventHelper.insert (
			simulatorEventHelper.createInstance ()

			.setSimulatorSession (
				simulatorSession)

			.setType (
				"delivery_report")

			.setTimestamp (
				transaction.now ())

			.setData (
				JSONValue.toJSONString (data)));

		// done

		transaction.commit ();

		return jsonResponder.get ()

			.value (
				ImmutableMap.<Object,Object>builder ()

				.put (
					"success",
					true)

				.build ()

			);

	}

	Optional<RouteRec> resolveRoute (
			SimulatorRec simulator,
			String number) {

		for (
			SimulatorRouteRec simulatorRoute
				: simulator.getSimulatorRoutes ()
		) {

			if (
				doesNotStartWith (
					number,
					simulatorRoute.getPrefix ())
			) {
				continue;
			}

			return Optional.of (
				simulatorRoute.getRoute ());

		}

		return Optional.absent ();

	}

	static
	class AjaxException
		extends RuntimeException {

		String message;

		AjaxException (
				String message) {

			this.message = message;

		}

	}

}
