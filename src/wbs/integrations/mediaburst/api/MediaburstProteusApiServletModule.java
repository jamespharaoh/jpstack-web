package wbs.integrations.mediaburst.api;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOfFormat;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.report.logic.SmsDeliveryReportLogic;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import wbs.web.context.RequestContext;
import wbs.web.file.AbstractWebFile;
import wbs.web.file.WebFile;
import wbs.web.pathhandler.PathHandler;
import wbs.web.pathhandler.RegexpPathHandler;
import wbs.web.responder.WebModule;

@SingletonComponent ("mediaburstProteusApiServletModule")
public
class MediaburstProteusApiServletModule
	implements WebModule {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SmsDeliveryReportLogic reportLogic;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
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
		void doPost (
				@NonNull TaskLogger parentTaskLogger) {

			try (

				OwnedTransaction transaction =
					database.beginReadWriteWithoutParameters (
						logContext,
						parentTaskLogger,
						"reportFile.doPost");

			) {

				ReportRequestResult reportRequestResult =
					processReportRequest (
						transaction,
						requestContext.inputStream ());

				if (
					isNull (
						reportRequestResult.status)
				) {
					return;
				}

				// lookup objects

				RouteRec route =
					routeHelper.findRequired (
						transaction,
						requestContext.requestIntegerRequired (
							"routeId"));

				reportLogic.deliveryReport (
					transaction,
					route,
					reportRequestResult.otherId,
					reportRequestResult.status,
					optionalOfFormat (
						reportRequestResult.statusString),
					optionalAbsent (),
					optionalOf (
						joinWithSpace (
							stringFormat (
								"status=%s",
								reportRequestResult.statusString),
							stringFormat (
								"errCode=%s",
								reportRequestResult.errCode))),
					optionalAbsent ());

				transaction.commit ();

			}

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

	ReportRequestResult processReportRequest (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull InputStream in) {

		try {

			ReportRequestResult reportRequestResult =
				new ReportRequestResult ();

			// decode xml

			Builder builder =
				new Builder ();

			Document document =
				builder.build (in);

			// save stuff

			reportRequestResult.messageId =
				Integer.parseInt (
					xomContent (
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
				parseIntegerRequired (
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
