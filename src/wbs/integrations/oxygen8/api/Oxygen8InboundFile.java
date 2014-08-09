package wbs.integrations.oxygen8.api;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.AbstractWebFile;
import wbs.framework.web.RequestContext;
import wbs.integrations.oxygen8.model.Oxygen8ConfigRec;
import wbs.integrations.oxygen8.model.Oxygen8NetworkObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8NetworkRec;
import wbs.integrations.oxygen8.model.Oxygen8RouteInObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8RouteInRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("oxygen8InboundFile")
public
class Oxygen8InboundFile
	extends AbstractWebFile {

	// dependencies

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	Oxygen8NetworkObjectHelper oxygen8NetworkHelper;

	@Inject
	Oxygen8RouteInObjectHelper oxygen8RouteInHelper;

	@Inject
	RequestContext requestContext;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	void doPost ()
		throws
			ServletException,
			IOException {

		State state =
			new State ();

		processRequest (state);

		updateDatabase (state);

		sendResponse (state);

	}

	void processRequest (
			State state) {

		state.routeId =
			requestContext.requestInt ("routeId");

		state.channel =
			requestContext.parameter ("Channel");

		state.reference =
			requestContext.parameter ("Reference");

		state.trigger =
			requestContext.parameter ("Trigger");

		state.shortcode =
			requestContext.parameter ("Shortcode");

		state.msisdn =
			requestContext.parameter ("MSISDN");

		state.content =
			requestContext.parameter ("Content");

		state.dataType =
			Integer.parseInt (
				requestContext.parameter ("DataType"));

		if (state.dataType != 0) {

			throw new RuntimeException (
				stringFormat (
					"Don't know how to handle data type %s",
					state.dataType));

		}

		state.dateReceived =
			Long.parseLong (
				requestContext.parameter ("DateReceived"));

		state.campaignId =
			Integer.parseInt (
				requestContext.parameter ("CampaignID"));

	}

	void updateDatabase (
			State state) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		RouteRec route =
			routeHelper.find (
				state.routeId);

		Oxygen8RouteInRec oxygen8RouteIn =
			oxygen8RouteInHelper.find (
				route.getId ());

		if (oxygen8RouteIn == null)
			throw new RuntimeException ();

		Oxygen8ConfigRec oxygen8Config =
			oxygen8RouteIn.getOxygen8Config ();

		Oxygen8NetworkRec oxygen8Network =
			oxygen8NetworkHelper.findByChannel (
				oxygen8Config,
				state.channel);

		if (oxygen8Network == null) {

			throw new RuntimeException (
				stringFormat (
					"Oxygen8 channel not recognised: %s",
					state.channel));

		}

		inboxLogic.inboxInsert (
			state.reference,
			textHelper.findOrCreate (state.content),
			state.msisdn,
			state.shortcode,
			route,
			oxygen8Network.getNetwork (),
			new Date (state.dateReceived),
			null,
			null,
			null);

		transaction.commit ();

	}

	void sendResponse (
			State state) {

		PrintWriter out =
			requestContext.writer ();

		out.print ("Success");

	}

	// state data structure

	static
	class State {

		int routeId;

		String channel;
		String reference;
		String trigger;
		String shortcode;
		String msisdn;
		String content;
		Integer dataType;
		Long dateReceived;
		Integer campaignId;

	}

}
