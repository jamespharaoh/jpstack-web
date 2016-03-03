package wbs.integrations.oxygen8.api;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.AbstractWebFile;
import wbs.framework.web.RequestContext;
import wbs.integrations.oxygen8.model.Oxygen8ReportCodeObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8ReportCodeRec;
import wbs.integrations.oxygen8.model.Oxygen8RouteOutObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8RouteOutRec;
import wbs.sms.message.report.logic.ReportLogic;
import wbs.sms.message.report.model.MessageReportCodeObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("oxygen8ReportFile")
public
class Oxygen8ReportFile
	extends AbstractWebFile {

	// dependencies

	@Inject
	Database database;

	@Inject
	MessageReportCodeObjectHelper messageReportCodeHelper;

	@Inject
	Oxygen8ReportCodeObjectHelper oxygen8ReportCodeHelper;

	@Inject
	Oxygen8RouteOutObjectHelper oxygen8RouteOutCodeHelper;

	@Inject
	ReportLogic reportLogic;

	@Inject
	RequestContext requestContext;

	@Inject
	RouteObjectHelper routeHelper;

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

		respondSuccess ();

	}

	void processRequest (
			State state) {

		state.routeId =
			requestContext.requestIntRequired (
				"routeId");

		state.reference =
			requestContext.parameter ("Reference");

		state.status =
			requestContext.parameter ("Status");

	}

	void updateDatabase (
			State state) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		Oxygen8RouteOutRec routeOut =
			oxygen8RouteOutCodeHelper.find (
				state.routeId);

		if (routeOut == null) {

			throw new RuntimeException (
				stringFormat (
					"No such route id %s",
					state.routeId));

		}

		if (! routeOut.getRoute ().getDeliveryReports ()) {

			throw new RuntimeException (
				stringFormat (
					"Delivery reports are not enabled for route %s.%s",
					routeOut.getRoute ().getSlice ().getCode (),
					routeOut.getRoute ().getCode ()));

		}

		Oxygen8ReportCodeRec reportCode =
			oxygen8ReportCodeHelper.findByCode (
				routeOut.getOxygen8Config (),
				state.status);

		if (reportCode == null) {

			throw new RuntimeException (
				stringFormat (
					"Unrecognised report status %s",
					state.status));

		}

		/*
		MessageReportCodeRec reportCode =
			messageReportCodeHelper.findOrCreate (
				state.status.hashCode (),
				null,
				null,
				MessageReportCodeType.oxygen8,
				state.reportCode.getMessageStatus ().isGoodType (),
				! state.reportCode.getMessageStatus ().isPending (),
				state.status);
		*/

		RouteRec route =
			routeHelper.find (
				state.routeId);

		reportLogic.deliveryReport (
			route,
			state.reference,
			reportCode.getMessageStatus (),
			null,
			null);

		transaction.commit ();

	}

	void respondSuccess () {

		PrintWriter out =
			requestContext.writer ();

		out.print (
			stringFormat (
				"SUCCESS\n"));

	}

	// state structure

	static
	class State {

		int routeId;

		String reference;
		String status;

	}

}
