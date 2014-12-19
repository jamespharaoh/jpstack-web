package wbs.test.simulator.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.toBoolean;
import static wbs.framework.utils.etc.Misc.toInteger;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import org.json.simple.JSONValue;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.scaffold.console.RootConsoleHelper;
import wbs.platform.scaffold.console.SliceConsoleHelper;
import wbs.platform.scaffold.model.RootRec;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.web.TextResponder;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.report.logic.ReportLogic;
import wbs.sms.network.console.NetworkConsoleHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.test.simulator.model.SimEventObjectHelper;
import wbs.test.simulator.model.SimEventRec;

import com.google.common.collect.ImmutableMap;

@PrototypeComponent ("simulatorCreateEventAction")
public
class SimulatorCreateEventAction
	extends ConsoleAction {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	NetworkConsoleHelper networkHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	ReportLogic reportLogic;

	@Inject
	RootConsoleHelper rootHelper;

	@Inject
	RouteConsoleHelper routeHelper;

	@Inject
	SimEventObjectHelper simEventHelper;

	@Inject
	SliceConsoleHelper sliceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	Provider<TextResponder> textResponder;

	@Override
	protected
	Responder backupResponder () {
		return null;
	}

	@Override
	protected
	Responder goReal () {

		String type =
			requestContext.getForm ("type");

		if (equal (type, "sendMessage"))
			return sendMessage ();

		if (equal (type, "deliveryReport"))
			return deliveryReport ();

		throw new RuntimeException ();

	}

	Responder sendMessage () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		String numFrom =
			requestContext.getForm ("numFrom");

		String numTo =
			requestContext.getForm ("numTo");

		String messageText =
			requestContext.getForm ("message");

		NetworkRec network =
			networkHelper.find (
				toInteger (
					requestContext.getForm ("networkId")));

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

		RootRec root =
			rootHelper.find (0);

		SliceRec testSlice =
			sliceHelper.findByCode (
				root,
				"test");

		RouteRec route = null;

		if (numTo.startsWith ("magic")) {

			route =
				routeHelper.findByCode (
					testSlice,
					"psychic_magic");

		}

		if (numTo.equals ("ps100")) {

			route =
				routeHelper.findByCode (
					testSlice,
					"psychic_1_00");

		}

		if (numTo.equals ("inbound")) {

			route =
				routeHelper.findByCode (
					testSlice,
					"inbound");

		}

		if (route == null)
			throw new NullPointerException ("ERROR");

		// insert inbox

		MessageRec message =
			inboxLogic.inboxInsert (
				null,
				textHelper.findOrCreate (messageText),
				numFrom,
				numTo,
				route,
				network,
				null,
				null,
				null,
				null);

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

		simEventHelper.insert (
			new SimEventRec ()
				.setType ("message_in")
				.setTimestamp (transaction.now ())
				.setData (JSONValue.toJSONString (data)));

		// done

		transaction.commit ();

		return textResponder.get ()
			.text ("ok");

	}

	Responder deliveryReport () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		Integer messageId =
			toInteger (requestContext.getForm ("messageId"));

		Boolean success =
			toBoolean (requestContext.getForm ("success"));

		// submit delivery report

		reportLogic.deliveryReport (
			messageId,
			success
				? MessageStatus.delivered
				: MessageStatus.undelivered,
			instantToDate (
				transaction.now ()),
			null);

		// create event data

		Object data =
			ImmutableMap.<String,Object>builder ()
				.put ("deliveryReport", ImmutableMap.<String,Object>builder ()
					.put ("messageId", messageId)
					.put ("success", success)
					.build ())
				.build ();

		// create event

		simEventHelper.insert (
			new SimEventRec ()
				.setType ("delivery_report")
				.setTimestamp (transaction.now ())
				.setData (JSONValue.toJSONString (data)));

		// done

		transaction.commit ();

		return textResponder.get ()
			.text ("ok");

	}

}
