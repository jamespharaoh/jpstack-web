package wbs.integrations.oxygen8.api;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.integrations.oxygen8.model.Oxygen8ConfigRec;
import wbs.integrations.oxygen8.model.Oxygen8NetworkObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8NetworkRec;
import wbs.integrations.oxygen8.model.Oxygen8RouteInObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8RouteInRec;
import wbs.platform.api.ApiAction;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.web.TextResponder;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("oxygen8InboundSmsAction")
public
class Oxygen8InboundSmsAction
	extends ApiAction {

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

	// prototype dependencies

	@Inject
	Provider<TextResponder> textResponderProvider;

	// state

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

	// implementation

	@Override
	protected
	Responder goApi () {

		processRequest ();

		updateDatabase ();

		return createResponse ();

	}

	void processRequest () {

		routeId =
			requestContext.requestInt ("routeId");

		channel =
			requestContext.parameter ("Channel");

		reference =
			requestContext.parameter ("Reference");

		trigger =
			requestContext.parameter ("Trigger");

		shortcode =
			requestContext.parameter ("Shortcode");

		msisdn =
			requestContext.parameter ("MSISDN");

		content =
			requestContext.parameter ("Content");

		dataType =
			Integer.parseInt (
				requestContext.parameter ("DataType"));

		if (dataType != 0) {

			throw new RuntimeException (
				stringFormat (
					"Don't know how to handle data type %s",
					dataType));

		}

		dateReceived =
			Long.parseLong (
				requestContext.parameter ("DateReceived"));

		campaignId =
			Integer.parseInt (
				requestContext.parameter ("CampaignID"));

	}

	void updateDatabase () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		RouteRec route =
			routeHelper.find (
				routeId);

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
				channel);

		if (oxygen8Network == null) {

			throw new RuntimeException (
				stringFormat (
					"Oxygen8 channel not recognised: %s",
					channel));

		}

		inboxLogic.inboxInsert (
			reference,
			textHelper.findOrCreate (content),
			msisdn,
			shortcode,
			route,
			oxygen8Network.getNetwork (),
			new Date (dateReceived),
			null,
			null,
			null);

		transaction.commit ();

	}

	Responder createResponse () {

		return textResponderProvider.get ()
			.text ("success");

	}

}
