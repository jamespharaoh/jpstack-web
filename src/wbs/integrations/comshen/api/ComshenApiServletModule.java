package wbs.integrations.comshen.api;

import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;
import java.util.regex.Matcher;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
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
import wbs.sms.message.report.logic.SmsDeliveryReportLogic;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("comshenApiServletModule")
public
class ComshenApiServletModule
	implements ServletModule {

	// singleton properties

	@SingletonDependency
	CommandLogic commandLogic;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogLogic exceptionLogic;

	@SingletonDependency
	MessageDao messageDao;

	@SingletonDependency
	SmsDeliveryReportLogic reportLogic;

	@SingletonDependency
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
					"ComshenApiServletModule.reportFile.doGet ()",
					this);

			Long routeId =
				requestContext.requestIntegerRequired (
					"routeId");

			String idParam =
				requestContext.parameterOrNull (
					"id");

			String statParam =
				requestContext.parameterOrNull (
					"stat");

			String errParam =
				requestContext.parameterOrNull (
					"err");

			RouteRec route =
				routeHelper.findRequired (
					routeId);

			MessageStatus result =
				statToResult.get (
					statParam);

			// process delivery report

			reportLogic.deliveryReport (
				route,
				idParam,
				result,
				Optional.of (
					statParam),
				Optional.absent (),
				Optional.of (
					joinWithSpace (
						stringFormat (
							"stat=%s",
							statParam),
						stringFormat (
							"err=%s",
							errParam))),
				Optional.absent ());

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
