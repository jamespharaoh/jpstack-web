package wbs.integrations.oxygen8.api;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.servlet.ServletException;

import com.google.common.base.Optional;

import lombok.Cleanup;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.AbstractWebFile;
import wbs.framework.web.RequestContext;
import wbs.integrations.oxygen8.model.Oxygen8ReportCodeObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8ReportCodeRec;
import wbs.integrations.oxygen8.model.Oxygen8RouteOutObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8RouteOutRec;
import wbs.sms.message.report.logic.SmsDeliveryReportLogic;
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
	Oxygen8ReportCodeObjectHelper oxygen8ReportCodeHelper;

	@Inject
	Oxygen8RouteOutObjectHelper oxygen8RouteOutCodeHelper;

	@Inject
	SmsDeliveryReportLogic reportLogic;

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
			requestContext.requestIntegerRequired (
				"routeId");

		state.reference =
			requestContext.parameterOrNull (
				"Reference");

		state.status =
			requestContext.parameterOrNull (
				"Status");

	}

	void updateDatabase (
			State state) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"Oxygen8ReportFile.updateDatabase (state)",
				this);

		Oxygen8RouteOutRec routeOut =
			oxygen8RouteOutCodeHelper.findRequired (
				state.routeId);

		if (! routeOut.getRoute ().getDeliveryReports ()) {

			throw new RuntimeException (
				stringFormat (
					"Delivery reports are not enabled for route %s.%s",
					routeOut.getRoute ().getSlice ().getCode (),
					routeOut.getRoute ().getCode ()));

		}

		Oxygen8ReportCodeRec reportCode =
			oxygen8ReportCodeHelper.findByCodeRequired (
				routeOut.getOxygen8Config (),
				state.status);

		RouteRec route =
			routeHelper.findRequired (
				state.routeId);

		reportLogic.deliveryReport (
			route,
			state.reference,
			reportCode.getMessageStatus (),
			Optional.of (
				state.status),
			Optional.of (
				reportCode.getDescription ()),
			Optional.absent (),
			Optional.absent ());

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

		Long routeId;

		String reference;
		String status;

	}

}
