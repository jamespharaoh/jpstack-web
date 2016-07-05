package wbs.integrations.broadcastsystems.api;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.AbstractWebFile;
import wbs.framework.web.RequestContext;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.report.logic.ReportLogic;
import wbs.sms.message.report.model.MessageReportCodeObjectHelper;
import wbs.sms.message.report.model.MessageReportCodeRec;
import wbs.sms.message.report.model.MessageReportCodeType;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

// TODO this doesn't look right...

@SingletonComponent ("broadcastSystemsReportFile")
public
class BroadcastSystemsReportFile
	extends AbstractWebFile {

	@Inject
	RequestContext requestContext;

	@Inject
	Database database;

	@Inject
	MessageReportCodeObjectHelper messageReportCodeHelper;

	@Inject
	ReportLogic reportLogic;

	@Inject
	RouteObjectHelper routeHelper;

	@Override
	public
	void doGet ()
		throws
			ServletException,
			IOException {

		Data data =
			new Data ();

		processRequest (
			data);

		updateDatabase (
			data);

	}

	public
	void processRequest (
			Data data) {

		data.routeId =
			requestContext.requestIntRequired (
				"routeId");

		data.transactionId =
			requestContext.parameter ("message_id");

		data.statusCode =
			requestContext.parameter ("status");

		if (equal (data.statusCode, "delivered")) {
			data.status = MessageStatus.delivered;
		} else if (equal (data.statusCode, "rejected_by_handset")) {
			data.status = MessageStatus.undelivered;
		} else if (equal (data.statusCode, "pending")) {
			data.status = MessageStatus.submitted;
		} else if (equal (data.statusCode, "accepted_by_smsc")) {
			data.status = MessageStatus.submitted;
		} else if (equal (data.statusCode, "rejected_by_smsc")) {
			data.status = MessageStatus.undelivered;
		} else if (equal (data.statusCode, "undeliverable")) {
			data.status = MessageStatus.undelivered;
		} else if (equal (data.statusCode, "rejected")) {
			data.status = MessageStatus.undelivered;
		} else if (equal (data.statusCode, "failed")) {
			data.status = MessageStatus.undelivered;
		} else if (equal (data.statusCode, "expired")) {
			data.status = MessageStatus.undelivered;
		} else {

			throw new RuntimeException (
				stringFormat (
					"Unrecognised report status: %s",
					data.statusCode));

		}

	}

	void updateDatabase (
			Data data) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		MessageReportCodeRec reportCode =
			messageReportCodeHelper.findOrCreate (
				(long) data.statusCode.hashCode (),
				null,
				null,
				MessageReportCodeType.broadcastSystems,
				data.status.isGoodType (),
				! data.status.isPending (),
				data.statusCode);

		RouteRec route =
			routeHelper.findOrNull (
				data.routeId);

		reportLogic.deliveryReport (
			route,
			data.transactionId,
			Optional.of (
				data.status),
			null,
			reportCode);

		transaction.commit ();

	}

	static
	class Data {

		int routeId;
		String transactionId;
		MessageStatus status;
		String statusCode;

	}

}