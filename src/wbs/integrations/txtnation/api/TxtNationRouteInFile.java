package wbs.integrations.txtnation.api;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.PrintWriter;
import java.util.Collections;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import org.joda.time.Instant;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.etc.StringFormatter;
import wbs.framework.web.AbstractWebFile;
import wbs.framework.web.RequestContext;
import wbs.integrations.txtnation.model.TxtNationRouteInObjectHelper;
import wbs.integrations.txtnation.model.TxtNationRouteInRec;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;

@Log4j
@SingletonComponent ("txtNationRouteInFile")
public
class TxtNationRouteInFile
	extends AbstractWebFile {

	// dependencies

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	NumberFormatLogic numberFormatLogic;

	@Inject
	RequestContext requestContext;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	TxtNationRouteInObjectHelper txtNationRouteInHelper;

	// implementation

	@Override
	public
	void doPost () {

		int routeId =
			requestContext.requestIntRequired (
				"routeId");

		String actionParam =
			requestContext.parameter ("action");

		String idParam =
			requestContext.parameter ("id");

		String numberParam =
			requestContext.parameter ("number");

		@SuppressWarnings ("unused")
		String networkParam =
			requestContext.parameter ("network");

		String messageParam =
			requestContext.parameter ("message");

		String shortcodeParam =
			requestContext.parameter ("shortcode");

		String countryParam =
			requestContext.parameter ("country");

		@SuppressWarnings ("unused")
		String billingParam =
			requestContext.parameter ("billing");

		// debugging

		requestContext.debugDump (
			log);

		// start transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"TxtNationRouteInFile.doPost ()",
				this);

		TxtNationRouteInRec txtNationRouteIn =
			txtNationRouteInHelper.findOrThrow (
				routeId,
				() -> new RuntimeException (
					stringFormat (
						"No txtNation inbound route info for route %s",
						routeId)));

		// sanity checks

		if (! equal (
				actionParam,
				"mpush_ir_message")) {

			throw new RuntimeException (
				stringFormat (
					"Got unrecognised action: %s",
					actionParam));

		}

		if (! equal (
				countryParam,
				"UK")) {

			throw new RuntimeException (
				stringFormat (
					"Got unrecognised country: %s",
					countryParam));

		}

		String numberFrom;

		try {

			numberFrom =
				numberFormatLogic.parse (
					txtNationRouteIn.getNumberFormat (),
					numberParam);

		} catch (WbsNumberFormatException exception) {

			throw new RuntimeException (
				stringFormat (
					"Invalid number: %s",
					numberParam));

		}

		String numberTo;

		try {

			numberTo =
				numberFormatLogic.parse (
					txtNationRouteIn.getNumberFormat (),
					shortcodeParam);

		} catch (WbsNumberFormatException exception) {

			throw new RuntimeException (
				stringFormat (
					"Invalid shortcode: %s",
					shortcodeParam));

		}

		// store message

		inboxLogic.inboxInsert (
			Optional.of (idParam),
			textHelper.findOrCreate (messageParam),
			numberFrom,
			numberTo,
			txtNationRouteIn.getRoute (),
			Optional.<NetworkRec>absent (),
			Optional.<Instant>absent (),
			Collections.<MediaRec>emptyList (),
			Optional.<String>absent (),
			Optional.<String>absent ());

		// commit

		transaction.commit ();

		// send response

		PrintWriter out =
			requestContext.writer ();

		StringFormatter.printWriterFormat (
			out,
			"OK\n");

	}

}
