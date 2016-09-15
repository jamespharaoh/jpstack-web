package wbs.integrations.broadcastsystems.api;

import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.IOException;

import javax.servlet.ServletException;

import com.google.common.base.Optional;

import lombok.Cleanup;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.AbstractWebFile;
import wbs.framework.web.RequestContext;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.report.logic.SmsDeliveryReportLogic;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

// TODO this doesn't look right...

@SingletonComponent ("broadcastSystemsReportFile")
public
class BroadcastSystemsReportFile
	extends AbstractWebFile {

	// singleton dependencies

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	SmsDeliveryReportLogic reportLogic;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	// implementation

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
			requestContext.requestIntegerRequired (
				"routeId");

		data.transactionId =
			requestContext.parameterOrNull ("message_id");

		data.statusCode =
			requestContext.parameterOrNull ("status");

		if (stringEqualSafe (data.statusCode, "delivered")) {
			data.status = MessageStatus.delivered;
		} else if (stringEqualSafe (data.statusCode, "rejected_by_handset")) {
			data.status = MessageStatus.undelivered;
		} else if (stringEqualSafe (data.statusCode, "pending")) {
			data.status = MessageStatus.submitted;
		} else if (stringEqualSafe (data.statusCode, "accepted_by_smsc")) {
			data.status = MessageStatus.submitted;
		} else if (stringEqualSafe (data.statusCode, "rejected_by_smsc")) {
			data.status = MessageStatus.undelivered;
		} else if (stringEqualSafe (data.statusCode, "undeliverable")) {
			data.status = MessageStatus.undelivered;
		} else if (stringEqualSafe (data.statusCode, "rejected")) {
			data.status = MessageStatus.undelivered;
		} else if (stringEqualSafe (data.statusCode, "failed")) {
			data.status = MessageStatus.undelivered;
		} else if (stringEqualSafe (data.statusCode, "expired")) {
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
				"BroadcastSystemsReportFile.updateDatabase (data)",
				this);

		RouteRec route =
			routeHelper.findRequired (
				data.routeId);

		reportLogic.deliveryReport (
			route,
			data.transactionId,
			data.status,
			Optional.of (
				data.statusCode),
			Optional.absent (),
			Optional.absent (),
			Optional.absent ());

		transaction.commit ();

	}

	static
	class Data {

		Long routeId;
		String transactionId;
		MessageStatus status;
		String statusCode;

	}

}