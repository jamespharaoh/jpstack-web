package wbs.integrations.txtnation.api;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import java.io.IOException;
import java.io.Writer;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.txtnation.model.TxtNationRouteInObjectHelper;
import wbs.integrations.txtnation.model.TxtNationRouteInRec;

import wbs.platform.text.model.TextObjectHelper;

import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;

import wbs.utils.io.RuntimeIoException;
import wbs.utils.string.FormatWriter;
import wbs.utils.string.WriterFormatWriter;

import wbs.web.context.RequestContext;
import wbs.web.file.AbstractWebFile;

@SingletonComponent ("txtNationRouteInFile")
public
class TxtNationRouteInFile
	extends AbstractWebFile {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberFormatLogic numberFormatLogic;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	NumberObjectHelper smsNumberHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	TxtNationRouteInObjectHelper txtNationRouteInHelper;

	// implementation

	@Override
	public
	void doPost (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"doPost");

		) {

			Long routeId =
				requestContext.requestIntegerRequired (
					"routeId");

			String actionParam =
				requestContext.parameterRequired (
					"action");

			String idParam =
				requestContext.parameterRequired (
					"id");

			String numberParam =
				requestContext.parameterRequired (
					"number");

			@SuppressWarnings ("unused")
			String networkParam =
				requestContext.parameterOrNull (
					"network");

			String messageParam =
				requestContext.parameterOrNull (
					"message");

			String shortcodeParam =
				requestContext.parameterOrNull (
					"shortcode");

			String countryParam =
				requestContext.parameterOrNull (
					"country");

			@SuppressWarnings ("unused")
			String billingParam =
				requestContext.parameterOrNull (
					"billing");

			// debugging

			requestContext.debugDump (
				transaction);

			// start transaction

			TxtNationRouteInRec txtNationRouteIn =
				txtNationRouteInHelper.findOrThrow (
					transaction,
					routeId,
					() -> new RuntimeException (
						stringFormat (
							"No txtNation inbound route info for route %s",
							integerToDecimalString (
								routeId))));

			// sanity checks

			if (
				stringNotEqualSafe (
					actionParam,
					"mpush_ir_message")
			) {

				throw new RuntimeException (
					stringFormat (
						"Got unrecognised action: %s",
						actionParam));

			}

			if (
				stringNotEqualSafe (
					countryParam,
					"UK")
			) {

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

			smsInboxLogic.inboxInsert (
				transaction,
				optionalOf (
					idParam),
				textHelper.findOrCreate (
					transaction,
					messageParam),
				smsNumberHelper.findOrCreate (
					transaction,
					numberFrom),
				numberTo,
				txtNationRouteIn.getRoute (),
				optionalAbsent (),
				optionalAbsent (),
				emptyList (),
				optionalAbsent (),
				optionalAbsent ());

			// commit

			transaction.commit ();

		}

		// send response

		try (

			Writer writer =
				requestContext.writer ();

			FormatWriter formatWriter =
				new WriterFormatWriter (
					writer);

		) {

			formatWriter.writeLineFormat (
				"OK");

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

}
