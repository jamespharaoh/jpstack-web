package wbs.integrations.dialogue.api;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.string.StringUtils.nullIfEmptyString;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.utils.time.TimeUtils.dateToInstantNullSafe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.apache.commons.fileupload.FileItem;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextObjectHelper;

import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.report.logic.SmsDeliveryReportLogic;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.utils.string.FormatWriter;

import wbs.web.context.RequestContext;
import wbs.web.file.AbstractWebFile;
import wbs.web.file.WebFile;
import wbs.web.pathhandler.PathHandler;
import wbs.web.pathhandler.RegexpPathHandler;
import wbs.web.responder.WebModule;

@SingletonComponent ("dialogueMmsApiServletModule")
public
class DialogueMmsApiServletModule
	implements WebModule {

	// TODO this is rather a big mess

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	SmsDeliveryReportLogic reportLogic;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	NumberObjectHelper smsNumberHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// TODO should be in the database
	Map <String, Integer> networks =
		ImmutableMap.<String,Integer>builder ()
			.put ("Orange UK", 1)
			.put ("Vodafone UK", 2)
			.put ("T-Mobile UK", 3)
			.put ("O2 UK", 4)
			.put ("Virgin UK", 5)
			.put ("3 UK", 6)
			.build ();

	public static
	SimpleDateFormat getDateFormat () {

		SimpleDateFormat ret =
			new SimpleDateFormat (
				"yyyy-MM-dd'T'HH:mm:ss");

		ret.setTimeZone (
			TimeZone.getTimeZone (
				"GMT"));

		return ret;

	}

	public final static
	Pattern contentTypePattern =
		Pattern.compile ("(\\S+); charset=(\\S+)");

	/** WebFile to handle incoming messages. */
	private
	WebFile inFile =
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
						"doPost");

			) {

				requestContext.debugDump (
					transaction);

				// process attachments

				String text =
					nullIfEmptyString (
						requestContext.headerOrEmptyString (
							"x-mms-subject"));

				List <MediaRec> medias =
					new ArrayList<> ();

				for (
					FileItem item
						: requestContext.fileItems ()
				) {

					Matcher matcher =
						contentTypePattern.matcher (
							item.getContentType ());

					if (! matcher.matches ()) {

						throw new RuntimeException (
							"Content type error");

					}

					String type =
						matcher.group (1);

					String charset =
						matcher.group (2);

					medias.add (
						mediaLogic.createMediaRequired (
							transaction,
							item.get (),
							type,
							item.getName (),
							optionalOf (
								charset)));

					if (

						isNull (
							text)

						&& stringEqualSafe (
							type,
							"text/plain")
					) {

						text =
							nullIfEmptyString (
								item.getString ());

					}

				}

				if (text == null)
					text = "";

				String mmsMessageId =
					requestContext.headerRequired (
						"x-mms-message-id");

				String mmsSenderAddress =
					requestContext.headerRequired (
						"x-mms-sender-address");

				String mmsRecipientAddress =
					requestContext.headerRequired (
						"x-mms-recipient-address");

				RouteRec route =
					routeHelper.findRequired (
						transaction,
						requestContext.requestIntegerRequired (
							"routeId"));

				Instant mmsDate;

				try {

					mmsDate =
						dateToInstantNullSafe (
							getDateFormat ().parse (
								requestContext.headerRequired (
									"x-mms-date")));

				} catch (ParseException parseException) {

					throw new RuntimeException (
						parseException);

				}

				String mmsSubject =
					requestContext.headerRequired (
						"x-mms-subject");

				// insert into inbox

				smsInboxLogic.inboxInsert (
					transaction,
					optionalOf (
						mmsMessageId),
					textHelper.findOrCreate (
						transaction,
						text),
					smsNumberHelper.findOrCreate (
						transaction,
						mmsSenderAddress),
					mmsRecipientAddress,
					route,
					optionalAbsent (),
					optionalOf (
						mmsDate),
					medias,
					optionalAbsent (),
					optionalFromNullable (
						mmsSubject));

				transaction.commit ();

				try (

					FormatWriter formatWriter =
						requestContext.formatWriter ();

				) {

					formatWriter.writeLineFormat (
						"OK");

				}

			}

		}

	};

	private
	WebFile reportFile =
		new AbstractWebFile() {

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

				if (requestContext.parameterMap ().size () == 0) {
					return;
				}

				// temporary, output all parameters

				transaction.debugFormat (
					"Parameter count %s",
					integerToDecimalString (
						requestContext.parameterMap ().size ()));

				for (
					Map.Entry <String, List <String>> entry
						: requestContext.parameterMap ().entrySet ()
				) {

					for (
						String value
							: entry.getValue ()
					) {

						transaction.debugFormat (
							"%s = %s",
							entry.getKey (),
							value);

					}

				}

				// int routeId = requestContext.getRequestInt ("routeId");

				String userKeyParam =
					requestContext.parameterRequired (
						"X-Mms-User-Key");

				Long messageId;

				try {

					messageId =
						Long.parseLong (
							userKeyParam);

				} catch (NumberFormatException exception) {

					throw new RuntimeException (
						stringFormat (
							"Ignoring dialogue MMS report with invalid user key, ",
							"X-Mms-User-Key=%s",
							userKeyParam));

				}

				MessageRec message =
					messageHelper.findRequired (
						transaction,
						messageId);

				if (
					stringNotEqualSafe (
						message.getMessageType ().getCode (),
						"mms")
				) {

					throw new RuntimeException (
						"Message is not MMS: " + messageId);

				}

				String deliveryReportParam =
					requestContext.parameterRequired (
						"X-Mms-Delivery-Report");

				MessageStatus newMessageStatus = null;

				long statusCode =
					Long.parseLong (
						deliveryReportParam,
						16);

				if (statusCode == 0) {

					newMessageStatus =
						MessageStatus.delivered;

				} else {

					newMessageStatus =
						MessageStatus.undelivered;

				}

				reportLogic.deliveryReport (
					transaction,
					message,
					newMessageStatus,
					optionalOf (
						deliveryReportParam),
					optionalAbsent (),
					optionalAbsent (),
					optionalAbsent ());

				transaction.commit ();

			}

		}

	};

	// ================================================================ entries

	final
	RegexpPathHandler.Entry routeEntry =
		new RegexpPathHandler.Entry (
			"/route/([0-9]+)/([^/]+)") {

		@Override
		protected
		WebFile handle (
				Matcher matcher) {

			requestContext.request (
				"routeId",
				Integer.parseInt (matcher.group (1)));

			return defaultFiles.get (
				matcher.group (2));

		}

	};

	// =========================================================== path handler

	final
	PathHandler pathHandler =
		new RegexpPathHandler (routeEntry);

	// ================================================================== files

	final
	Map<String,WebFile> defaultFiles =
		ImmutableMap.<String,WebFile>builder ()
			.put ("in", inFile)
			.put ("report", reportFile)
			.build ();

	// ========================================================= servlet module

	@Override
	public
	Map<String,PathHandler> paths () {

		return ImmutableMap.<String,PathHandler>builder ()

			.put (
				"/dialogueMMS",
				pathHandler)

			.build ();

	}

	@Override
	public
	Map<String,WebFile> files () {

		return null;

	}

}
