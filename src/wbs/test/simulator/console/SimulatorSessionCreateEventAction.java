package wbs.test.simulator.console;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.LogicUtils.parseBooleanTrueFalseRequired;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.doesNotStartWithSimple;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.json.simple.JSONValue;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.scaffold.console.RootConsoleHelper;
import wbs.platform.scaffold.console.SliceConsoleHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.console.UserSessionLogic;
import wbs.platform.user.model.UserRec;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.report.logic.SmsDeliveryReportLogic;
import wbs.sms.network.console.NetworkConsoleHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.console.NumberConsoleHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.test.simulator.model.SimulatorRec;
import wbs.test.simulator.model.SimulatorRouteRec;
import wbs.test.simulator.model.SimulatorSessionNumberRec;
import wbs.test.simulator.model.SimulatorSessionRec;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.Responder;

@PrototypeComponent ("simulatorSessionCreateEventAction")
public
class SimulatorSessionCreateEventAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NetworkConsoleHelper networkHelper;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	SmsDeliveryReportLogic reportLogic;

	@SingletonDependency
	RootConsoleHelper rootHelper;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	@SingletonDependency
	SimulatorEventConsoleHelper simulatorEventHelper;

	@SingletonDependency
	SimulatorSessionConsoleHelper simulatorSessionHelper;

	@SingletonDependency
	SimulatorSessionNumberConsoleHelper simulatorSessionNumberHelper;

	@SingletonDependency
	SliceConsoleHelper sliceHelper;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	NumberConsoleHelper smsNumberHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserSessionLogic userSessionLogic;

	// prototype dependencies

	@PrototypeDependency
	Provider <JsonResponder> jsonResponderProvider;

	// details

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"goReal");

		) {

			try {

				String type =
					requestContext.formRequired (
						"type");

				if (
					stringEqualSafe (
						type,
						"sendMessage")
				) {

					return sendMessage (
						taskLogger);

				}

				if (
					stringEqualSafe (
						type,
						"deliveryReport")
				) {

					return deliveryReport (
						taskLogger);

				}

				throw new AjaxException (
					stringFormat (
						"Invalid event type: %s",
						type));

			} catch (AjaxException error) {

				return jsonResponderProvider.get ()

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
					taskLogger,
					"console",
					requestContext.requestPath (),
					exception,
					optionalOf (
						userConsoleLogic.userIdRequired ()),
					GenericExceptionResolution.ignoreWithUserWarning);

				return jsonResponderProvider.get ()

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

	}

	Responder sendMessage (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithoutParameters (
					logContext,
					parentTaskLogger,
					"sendMessage");

		) {

			UserRec user =
				userConsoleLogic.userRequired (
					transaction);

			SimulatorSessionRec simulatorSession =
				simulatorSessionHelper.findFromContextRequired (
					transaction);

			SimulatorRec simulator =
				simulatorSession.getSimulator ();

			String numFrom =
				requestContext.formRequired (
					"numFrom");

			String numTo =
				requestContext.formRequired (
					"numTo");

			String messageText =
				requestContext.formRequired (
					"message");

			NetworkRec network =
				networkHelper.findRequired (
					transaction,
					parseIntegerRequired (
						requestContext.formRequired (
							"networkId")));

			// store in session

			userSessionLogic.userDataStringStore (
				transaction,
				user,
				"simulator_num_from",
				numFrom);

			userSessionLogic.userDataStringStore (
				transaction,
				user,
				"simulator_num_to",
				numTo);

			userSessionLogic.userDataStringStore (
				transaction,
				user,
				"simulator_message",
				messageText);

			userSessionLogic.userDataStringStore (
				transaction,
				user,
				"simulator_network_id",
				network.getId ().toString ());

			// work out route

			Optional <RouteRec> routeOption =
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
				smsInboxLogic.inboxInsert (
					transaction,
					optionalAbsent (),
					textHelper.findOrCreate (
						transaction,
						messageText),
					smsNumberHelper.findOrCreate (
						transaction,
						numFrom),
					numTo,
					route,
					optionalOf (
						network),
					optionalAbsent (),
					emptyList (),
					optionalAbsent (),
					optionalAbsent ());

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
				transaction,
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

			transaction.flush ();

			NumberRec number =
				smsNumberHelper.findOrCreate (
					transaction,
					numFrom);

			SimulatorSessionNumberRec simulatorSessionNumber =
				simulatorSessionNumberHelper.findOrCreate (
					transaction,
					number);

			simulatorSessionNumber

				.setSimulatorSession (
					simulatorSession);

			// done

			transaction.commit ();

			return jsonResponderProvider.get ()
				.value (
					ImmutableMap.<Object,Object>builder ()
						.put ("success", true)
						.build ());

		}

	}

	Responder deliveryReport (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithoutParameters (
					logContext,
					parentTaskLogger,
					"deliveryReport");

		) {

			SimulatorSessionRec simulatorSession =
				simulatorSessionHelper.findFromContextRequired (
					transaction);

			Long messageId =
				parseIntegerRequired (
					requestContext.formRequired (
						"messageId"));

			Boolean success =
				parseBooleanTrueFalseRequired (
					requestContext.formRequired (
						"success"));

			// submit delivery report

			reportLogic.deliveryReport (
				transaction,
				messageId,
				ifThenElse (
					success,
					() -> MessageStatus.delivered,
					() -> MessageStatus.undelivered),
				Optional.absent (),
				Optional.absent (),
				Optional.absent (),
				Optional.absent ());

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
				transaction,
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

			return jsonResponderProvider.get ()

				.value (
					ImmutableMap.<Object,Object>builder ()

					.put (
						"success",
						true)

					.build ()

				);

		}

	}

	Optional<RouteRec> resolveRoute (
			SimulatorRec simulator,
			String number) {

		for (
			SimulatorRouteRec simulatorRoute
				: simulator.getSimulatorRoutes ()
		) {

			if (
				doesNotStartWithSimple (
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
