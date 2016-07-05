package wbs.integrations.mediaburst.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.AbstractWebFile;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RegexpPathHandler;
import wbs.framework.web.RequestContext;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.report.logic.ReportLogic;
import wbs.sms.message.report.model.MessageReportCodeObjectHelper;
import wbs.sms.message.report.model.MessageReportCodeRec;
import wbs.sms.message.report.model.MessageReportCodeType;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@Log4j
@SingletonComponent ("mediaburstProteusApiServletModule")
public
class MediaburstProteusApiServletModule
	implements ServletModule {

	// ============================================================ properties

	@Inject
	Database database;

	@Inject
	MessageReportCodeObjectHelper messageReportCodeHelper;

	@Inject
	ReportLogic reportLogic;

	@Inject
	RequestContext requestContext;

	@Inject
	RouteObjectHelper routeHelper;

	// ============================================================ inFile

	static
	String xomContent (
			Document doc,
			String path) {

		Nodes nodes =
			doc.query (path);

		Element element =
			(Element) nodes.get (0);

		return element.getValue ();

	}

	static
	Map<String,MessageStatus> stringToMessageStatus =
		ImmutableMap.<String,MessageStatus>builder ()
			.put ("enroute", MessageStatus.submitted)
			.put ("delivrd", MessageStatus.delivered)
			.put ("expired", MessageStatus.undelivered)
			.put ("deleted", MessageStatus.undelivered)
			.put ("undeliv", MessageStatus.undelivered)
			.put ("acceptd", MessageStatus.submitted)
			.put ("rejectd", MessageStatus.undelivered)
			.build ();

	static
	List<String> allMessageStatuses =
		ImmutableList.<String>of (
			"queued",
			"enroute",
			"delivrd",
			"expired",
			"deleted",
			"undeliv",
			"acceptd",
			"unknown",
			"rejectd");

	WebFile reportFile =
		new AbstractWebFile () {

		@Override
		public
		void doPost ()
			throws ServletException,
				IOException {

			ReportRequestResult reportRequestResult =
				processReportRequest (
					requestContext.inputStream ());

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					this);

			Long statusCode;

			try {

				statusCode =
					Long.parseLong (
						reportRequestResult.errCode);

			} catch (NumberFormatException exception) {

				statusCode = null;

			}

			Long statusType = null;
			Long reason = null;

			statusType =
				(long) (int)
				allMessageStatuses.indexOf (
					reportRequestResult.statusString.toLowerCase ());

			MessageReportCodeRec messageReportCode =
				messageReportCodeHelper.findOrCreate (
					statusCode,
					statusType,
					reason,
					MessageReportCodeType.mediaburst,
					reportRequestResult.status == MessageStatus.delivered,
					false,
					reportRequestResult.statusString);

			RouteRec route =
				routeHelper.findOrNull (
					requestContext.requestIntRequired (
						"routeId"));

			reportLogic.deliveryReport (
				route,
				reportRequestResult.otherId,
				Optional.fromNullable (
					reportRequestResult.status),
				null,
				messageReportCode);

			transaction.commit ();

		}

	};

	static
	class ReportRequestResult {

		int messageId;

		String otherId;
		MessageStatus status;
		String statusString;
		String errCode;

	}

	static ReportRequestResult processReportRequest (
			InputStream in) {

		try {

			ReportRequestResult reportRequestResult =
				new ReportRequestResult ();

			// decode xml

			Builder builder =
				new Builder ();

			Document document =
				builder.build (in);

			log.debug (
				document.toXML ());

			// save stuff

			reportRequestResult.messageId =
				Integer.parseInt(xomContent (
					document,
					"/DeliveryReceipt/ClientID"));

			reportRequestResult.otherId =
				xomContent (
					document,
					"/DeliveryReceipt/MsgID");

			String statusString =
				xomContent (
					document,
					"/DeliveryReceipt/Status");

			reportRequestResult.errCode =
				xomContent (
					document,
					"/DeliveryReceipt/ErrCode");

			// ret.code = statusString + "/" + errCode;

			// work out status

			if (! allMessageStatuses.contains (
					statusString.toLowerCase ())) {

				throw new RuntimeException (
					"Unknown message status: " + statusString);

			}

			reportRequestResult.status =
				stringToMessageStatus.get (
					statusString.toLowerCase ());

			reportRequestResult.statusString =
				statusString;

			return reportRequestResult;

		} catch (Exception exception) {

			throw new RuntimeException (exception);

		}

	}

	// ================================================================ entries

	RegexpPathHandler.Entry routeEntry =
		new RegexpPathHandler.Entry (
			"/route/([0-9]+)/([^/]+)") {

		@Override
		protected
		WebFile handle (
				Matcher matcher) {

			requestContext.request (
				"routeId",
				Integer.parseInt (
					matcher.group (1)));

			return defaultFiles.get (
				matcher.group (2));

		}

	};

	// ================================= path handler

	PathHandler pathHandler =
		new RegexpPathHandler (
			routeEntry);

	// ================================= files

	Map<String,WebFile> defaultFiles =
		ImmutableMap.<String,WebFile>builder ()
			.put ("report", reportFile)
			.build ();

	// ================================= servlet module

	@Override
	public
	Map<String,PathHandler> paths () {

		return ImmutableMap.<String,PathHandler>builder ()
			.put ("/mediaburst/proteus", pathHandler)
			.build ();

	}

	@Override
	public
	Map<String,WebFile> files () {
		return null;
	}

}
