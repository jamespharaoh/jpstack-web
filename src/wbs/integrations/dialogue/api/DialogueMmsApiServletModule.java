package wbs.integrations.dialogue.api;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.nullIfEmptyString;
import static wbs.framework.utils.etc.TimeUtils.dateToInstantNullSafe;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import org.apache.commons.fileupload.FileItem;
import org.joda.time.Instant;

import com.google.common.base.Optional;
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
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.report.logic.ReportLogic;
import wbs.sms.message.report.model.MessageReportCodeObjectHelper;
import wbs.sms.message.report.model.MessageReportCodeRec;
import wbs.sms.message.report.model.MessageReportCodeType;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@Log4j
@SingletonComponent ("dialogueMmsApiServletModule")
public
class DialogueMmsApiServletModule
	implements ServletModule {

	// TODO this is rather a big mess

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MediaLogic mediaLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	MessageReportCodeObjectHelper messageReportCodeHelper;

	@Inject
	ReportLogic reportLogic;

	@Inject
	RequestContext requestContext;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	TextObjectHelper textHelper;

	// TODO should be in the database
	Map<String,Integer> networks =
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
		void doPost ()
			throws
				ServletException,
				IOException {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					"DialogueMmsApiServletModule.inFile.doPost ()",
					this);

			requestContext.debugDump (log);

			// process attachments

			String text =
				nullIfEmptyString (
					requestContext.header (
						"x-mms-subject"));

			List<MediaRec> medias =
				new ArrayList<MediaRec> ();

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
						item.get (),
						type,
						item.getName (),
						Optional.of (
							charset)));

				if (
					text == null
					&& equal (
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
				requestContext.header (
					"x-mms-message-id");

			String mmsSenderAddress =
				requestContext.header (
					"x-mms-sender-address");

			String mmsRecipientAddress =
				requestContext.header (
					"x-mms-recipient-address");

			RouteRec route =
				routeHelper.findRequired (
					requestContext.requestIntRequired (
						"routeId"));

			Instant mmsDate;

			try {

				mmsDate =
					dateToInstantNullSafe (
						getDateFormat ().parse (
							requestContext.header (
								"x-mms-date")));

			} catch (ParseException parseException) {

				throw new RuntimeException (
					parseException);

			}

			String mmsSubject =
				requestContext.header (
					"x-mms-subject");

			// insert into inbox

			inboxLogic.inboxInsert (
				Optional.of (
					mmsMessageId),
				textHelper.findOrCreate (
					text),
				mmsSenderAddress,
				mmsRecipientAddress,
				route,
				Optional.<NetworkRec>absent (),
				Optional.of (
					mmsDate),
				medias,
				Optional.<String>absent (),
				Optional.fromNullable (
					mmsSubject));

			transaction.commit ();

			PrintWriter out =
				requestContext.writer ();

			out.println ("OK");

		}

	};

	private
	WebFile reportFile =
		new AbstractWebFile() {

		@Override
		public
		void doPost ()
			throws ServletException,
				IOException {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					"DialogueMmsApiServletModule.reportFile.doPost ()",
					this);

			if (requestContext.parameterMap ().size () == 0) {
				return;
			}

			// temporary, output all parameters

			log.debug (
				stringFormat (
					"Parameter count %s",
					requestContext.parameterMap ().size ()));

			for (Map.Entry<String,List<String>> entry
					: requestContext.parameterMap ().entrySet ()) {

				for (String value : entry.getValue ())
					log.debug (entry.getKey () + " = " + value);

			}

			// int routeId = requestContext.getRequestInt ("routeId");

			String userKeyParam =
				requestContext.parameter (
					"X-Mms-User-Key");

			final int messageId;

			try {

				messageId =
					Integer.parseInt (
						userKeyParam);

			} catch (NumberFormatException exception) {

				throw new ServletException (
					stringFormat (
						"Ignoring dialogue MMS report with invalid user key, ",
						"X-Mms-User-Key=%s",
						userKeyParam));

			}

			MessageRec message =
				messageHelper.findRequired (
					messageId);

			if (
				notEqual (
					message.getMessageType ().getCode (),
					"mms")
			) {

				throw new ServletException (
					"Message is not MMS: " + messageId);

			}

			String deliveryReportParam =
				requestContext.parameter ("X-Mms-Delivery-Report");

			if (deliveryReportParam == null) {
				throw new ServletException("Unrecognised MMS report for "
						+ messageId);
			}

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

			Long statusType = null;
			Long reason = null;

			MessageReportCodeRec messageReportCode =
				messageReportCodeHelper.findOrCreate (
					statusCode,
					statusType,
					reason,
					MessageReportCodeType.dialogue,
					newMessageStatus == MessageStatus.delivered,
					false,
					null);

			reportLogic.deliveryReport (
				message,
				newMessageStatus,
				null,
				messageReportCode);

			transaction.commit ();

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
