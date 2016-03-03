package wbs.integrations.comshen.api;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Inject;

import lombok.Cleanup;

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
import wbs.platform.exception.logic.ExceptionLogLogic;
import wbs.sms.command.logic.CommandLogic;
import wbs.sms.message.core.model.MessageDao;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.report.logic.ReportLogic;
import wbs.sms.message.report.model.MessageReportCodeObjectHelper;
import wbs.sms.message.report.model.MessageReportCodeRec;
import wbs.sms.message.report.model.MessageReportCodeType;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("comshenApiServletModule")
public
class ComshenApiServletModule
	implements ServletModule {

	// ================================= properties

	@Inject
	CommandLogic commandLogic;

	@Inject
	RequestContext requestContext;

	@Inject
	Database database;

	@Inject
	ExceptionLogLogic exceptionLogic;

	@Inject
	MessageDao messageDao;

	@Inject
	MessageReportCodeObjectHelper messageReportCodeHelper;

	@Inject
	ReportLogic reportLogic;

	@Inject
	RouteObjectHelper routeHelper;

	// ================================= report file

	public final static
	Map<String,MessageStatus> statToResult =
		ImmutableMap.<String,MessageStatus>builder ()
			.put ("DELIBRD", MessageStatus.delivered) // TODO sp?
			.put ("EXPIRED", MessageStatus.undelivered)
			.put ("DELETED", MessageStatus.undelivered)
			.put ("UNDELIV", MessageStatus.undelivered)
			.put ("ACCEPTD", MessageStatus.submitted)
			.put ("REJECTD", MessageStatus.undelivered)
			.build ();

	private
	WebFile reportFile =
		new AbstractWebFile () {

		@Override
		public
		void doGet () {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					this);

			int routeId =
				requestContext.requestIntRequired (
					"routeId");

			String idParam =
				requestContext.parameter ("id");

			String statParam =
				requestContext.parameter ("stat");

			String errParam =
				requestContext.parameter ("err");

			RouteRec route =
				routeHelper.find (routeId);

			if (route == null) {

				throw new RuntimeException (
					stringFormat (
						"Route not found: %s",
						routeId));

			}

			MessageStatus result =
				statToResult.get (statParam);

			// update message report code

			MessageReportCodeRec messageReportCode =
				messageReportCodeHelper.findOrCreate (
					null,
					null,
					null,
					MessageReportCodeType.comshen,
					result != null
						? result.isGoodType ()
						: false,
					false,
					stringFormat (
						"%s / %s",
						statParam,
						errParam));

			// process delivery report

			reportLogic.deliveryReport (
				route,
				idParam,
				result,
				null,
				messageReportCode);

			transaction.commit ();

		}

	};

	// ============================================================ entries

	final RegexpPathHandler.Entry routeEntry =
		new RegexpPathHandler.Entry ("/route/([0-9]+)/([^/]+)") {

		@Override
		protected WebFile handle (
				Matcher matcher) {

			requestContext.request (
				"routeId",
				Integer.parseInt (matcher.group (1)));

			return defaultFiles.get (
				matcher.group (2));

		}

	};

	// ============================================================ path handler

	final
	PathHandler pathHandler =
		new RegexpPathHandler (
			routeEntry);

	// ============================================================ files

	final
	Map<String,WebFile> defaultFiles =
		ImmutableMap.<String,WebFile>builder ()
			.put ("report", reportFile)
			.build ();

	// ============================================================ servlet module

	@Override
	public
	Map<String,PathHandler> paths () {

		return ImmutableMap.<String,PathHandler>builder ()
			.put ("/comshen", pathHandler)
			.build ();

	}

	@Override
	public
	Map<String,WebFile> files () {

		return ImmutableMap.<String,WebFile>builder ()
			.build ();

	}

}
