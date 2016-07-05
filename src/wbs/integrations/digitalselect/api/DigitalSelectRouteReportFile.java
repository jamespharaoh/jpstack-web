package wbs.integrations.digitalselect.api;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.AbstractWebFile;
import wbs.framework.web.RequestContext;
import wbs.integrations.digitalselect.model.DigitalSelectRouteOutObjectHelper;
import wbs.integrations.digitalselect.model.DigitalSelectRouteOutRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.core.logic.NoSuchMessageException;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.report.logic.ReportLogic;

@Log4j
@SingletonComponent ("digitalSelectRouteReportFile")
public
class DigitalSelectRouteReportFile
	extends AbstractWebFile {

	// dependencies

	@Inject
	Database database;

	@Inject
	DigitalSelectRouteOutObjectHelper digitalSelectRouteOutHelper;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	ReportLogic reportLogic;

	@Inject
	RequestContext requestContext;

	@Inject
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	void doPost ()
		throws IOException {

		int routeId =
			requestContext.requestIntRequired (
				"routeId");

		String msgidParam =
			requestContext.parameter ("msgid");

		String statParam =
			requestContext.parameter ("stat");

		// debugging

		requestContext.debugDump (
			log);

		// sanity checks

		if (! messageStatusCodes.containsKey (statParam)) {

			throw new RuntimeException (
				stringFormat (
					"Unrecognised result: %s",
					statParam));

		}

		MessageStatus newMessageStatus =
			messageStatusCodes.get (statParam);

		// start transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		DigitalSelectRouteOutRec digitalSelectRouteOut =
			digitalSelectRouteOutHelper.findOrNull (
				routeId);

		if (digitalSelectRouteOut == null) {

			throw new RuntimeException (
				stringFormat (
					"No Digital Select inbound route info for route %s",
					routeId));

		}


		// store report

		try {

			reportLogic.deliveryReport (
				digitalSelectRouteOut.getRoute (),
				msgidParam,
				Optional.of (
					newMessageStatus),
				null,
				null);

		} catch (NoSuchMessageException exception) {

			// handle regular unrecognised message ids with a log warning and
			// custom HTTP response code

			// TODO expose frequent errors like this better somehow

			log.warn (
				stringFormat (
					"Received delivery report for unknown message %s",
					msgidParam));

			requestContext.sendError (
				409,
				"Message does not exist");

			return;

		}

		// commit

		transaction.commit ();

	}

	// data

	final static
	Map<String,MessageStatus> messageStatusCodes =
		ImmutableMap.<String,MessageStatus>builder ()
			.put ("acked", MessageStatus.submitted)
			.put ("buffered phone", MessageStatus.submitted)
			.put ("buffered smsc", MessageStatus.submitted)
			.put ("Delivered", MessageStatus.delivered)
			.put ("Undelivered", MessageStatus.undelivered)
			.put ("Non Delivered", MessageStatus.undelivered)
			.put ("Lost Notification", MessageStatus.undelivered)
			.build ();

}
