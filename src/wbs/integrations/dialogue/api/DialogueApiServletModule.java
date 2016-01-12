package wbs.integrations.dialogue.api;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.joda.time.Instant;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import wbs.api.mvc.ApiFile;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.AbstractWebFile;
import wbs.framework.web.Action;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RegexpPathHandler;
import wbs.framework.web.RegexpPathHandler.Entry;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.gsm.ConcatenatedInformationElement;
import wbs.sms.gsm.UserDataHeader;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.logic.InboxMultipartLogic;
import wbs.sms.message.report.logic.ReportLogic;
import wbs.sms.message.report.model.MessageReportCodeObjectHelper;
import wbs.sms.message.report.model.MessageReportCodeRec;
import wbs.sms.message.report.model.MessageReportCodeType;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@Log4j
@SingletonComponent ("dialogueApiServletModule")
public
class DialogueApiServletModule
	implements ServletModule {

	// dependencies

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	InboxMultipartLogic inboxMultipartLogic;

	@Inject
	MessageReportCodeObjectHelper messageReportCodeHelper;

	@Inject
	NetworkObjectHelper networkHelper;

	@Inject
	ReportLogic reportLogic;

	@Inject
	RequestContext requestContext;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	TextObjectHelper textHelper;

	// prototype dependencies

	@Inject
	Provider<ApiFile> apiFile;

	@Inject
	Provider<DialogueResponder> dialogueResponderProvider;

	// =============================================================== networks

	// TODO this belongs in the database

	private final static Map<String,Integer> networks =
		ImmutableMap.<String,Integer>builder ()
			.put ("Orange", 1)
			.put ("Vodafone", 2)
			.put ("One2One", 3)
			.put ("Cellnet", 4)
			.put ("Virgin", 5)
			.put ("ThreeUK", 6)
			.build ();

	// ============================================================ files

	/**
	 * WebFile to handle incoming messages. Correctly handles network
	 * translation, and also multipart messages.
	 *
	 * NB: The dialogue gateway translates the text part of the messages into
	 * latin1 but leaves the data header at the front intact, which makes for
	 * some interesting decoding.
	 */
	private WebFile inFile =
		new AbstractWebFile () {

		@Override
		public
		void doPost ()
			throws
				ServletException,
				IOException {

			// get request stuff

			int routeId =
				requestContext.requestInt ("route_id");

			// get params in local variables

			String numFromParam =
				requestContext.parameter ("X-E3-Originating-Address");

			String numToParam =
				requestContext.parameter ("X-E3-Recipients");

			String idParam =
				requestContext.parameter ("X-E3-ID");

			String networkParam =
				requestContext.parameter ("X-E3-Network");

			String hexMessageParam =
				requestContext.parameter ("X-E3-Hex-Message");

			String userDataHeaderIndicatorParam =
				requestContext.parameter ("X-E3-User-Data-Header-Indicator");

			String dataCodingSchemeParam =
				requestContext.parameter ("X-E3-Data-Coding-Scheme");

			// decode the network

			Integer networkId =
				networks.get (networkParam);

			// determine the character set from the data coding scheme

			int dataCodingScheme =
				Integer.parseInt (dataCodingSchemeParam, 16);

			String charset =
				((dataCodingScheme & 0x08) == 0x08)
					? "utf16"
					: "iso-8859-1";

			byte[] allBytes;

			try {

				allBytes =
					hexMessageParam != null
						? Hex.decodeHex (hexMessageParam.toCharArray ())
						: new byte [0];

			} catch (DecoderException exception) {

				throw new RuntimeException (exception);

			}

			// strip off the user data header (if present)

			byte[] messageBytes;

			UserDataHeader userDataHeader = null;

			if ("1".equals (userDataHeaderIndicatorParam)) {
				try {

					// get header

					if (allBytes.length == 0)
						throw new ServletException();

					byte[] headerBytes =
						new byte [(allBytes [0] & 0xFF) + 1];

					if (allBytes.length < headerBytes.length)
						throw new ServletException ();

					System.arraycopy (
						allBytes,
						0,
						headerBytes,
						0,
						headerBytes.length);

					log.debug ("udh.length = " + headerBytes.length);

					for (int i = 0; i < headerBytes.length; i++) {

						log.debug (
							"udh[" + i + "] = "	+ (headerBytes[i] & 0xff));

					}

					// and decode it

					userDataHeader =
						UserDataHeader.decode (ByteBuffer.wrap (headerBytes));

					// then find the message bytes

					messageBytes =
						new byte [allBytes.length - headerBytes.length];

					System.arraycopy (
						allBytes,
						headerBytes.length,
						messageBytes,
						0,
						messageBytes.length);

				} catch (Exception e) {

					log.debug (
						"Error decoding user data header " + e.getMessage ());

					requestContext.debugParameters (log);

					throw new ServletException (e);

					/*
					messageBytes = new String (
							"Unrecognised message content").getBytes();

					udh = null;
					*/

				}

			} else {

				messageBytes = allBytes;

			}

			// decode the message body

			String message;

			try {

				message =
					new String (messageBytes, charset);

			} catch (UnsupportedEncodingException exception) {

				throw new RuntimeException (
					exception);

			}

			// save the message

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					this);

			RouteRec route =
				routeHelper.find (routeId);

			NetworkRec network =
				networkId == null
					? null
					: networkHelper.find (networkId);

			// if it's a concatenated message...

			ConcatenatedInformationElement concat =
				userDataHeader != null
					? userDataHeader.find (ConcatenatedInformationElement.class)
					: null;

			if (concat != null) {

				// insert a part message

				inboxMultipartLogic.insertInboxMultipart (
					route,
					concat.getRef (),
					concat.getSeqMax (),
					concat.getSeqNum (),
					numToParam,
					numFromParam,
					null,
					network,
					idParam,
					message);

			} else {

				// insert a message

				inboxLogic.inboxInsert (
					Optional.of (idParam),
					textHelper.findOrCreate (message),
					numFromParam,
					numToParam,
					route,
					Optional.of (network),
					Optional.<Instant>absent (),
					Collections.<MediaRec>emptyList (),
					Optional.<String>absent (),
					Optional.<String>absent ());

			}

			transaction.commit ();

			PrintWriter out =
				requestContext.writer ();

			out.println ("<HTML>");
			out.println ("<!-- X-E3-Submission-Report: \"00\" -->");
			out.println ("</HTML>");

		}

	};

	private final
	Action reportAction =
		new Action () {

		@Override
		public
		Responder handle () {

			String idParam =
				requestContext.parameter ("X-E3-ID");

			String deliveryReportParam =
				requestContext.parameter ("X-E3-Delivery-Report");

			String submissionReportParam =
				requestContext.parameter ("X-E3-Submission-Report");

			String userKeyParam =
				requestContext.parameter ("X-E3-User-Key");

			// [TEMP] dump params

			for (Map.Entry<String,List<String>> entry
					: requestContext.parameterMap ().entrySet ()) {

				String name =
					entry.getKey ();

				List<String> values =
					entry.getValue ();

				for (String value : values)
					log.debug ("Param " + name + " = " + value);

			}

			// check for a user key

			if (userKeyParam == null) {

				log.warn (
					stringFormat (
						"Ignoring dialogue report with no user key, X-E3-ID=%s",
						idParam));

				return dialogueResponderProvider
					.get ();

			}

			final int messageId;

//			try {

				messageId =
					Integer.parseInt (userKeyParam);

//			} catch (NumberFormatException e) {

//				logger.warn (sf (
//					"Ignoring dialogue report with invalid user key, " +
//						"X-E3-ID=%s",
//					idParam));

//				return dialogueResponder;

//			}

			// work out the delivery report's meaning

			MessageStatus newMessageStatus = null;

			if (
				deliveryReportParam != null
				&& submissionReportParam != null
			) {

				throw new RuntimeException (
					"Got both delivery and submission reports!");

			}

			Long statusCode;

			if (deliveryReportParam != null) {

				statusCode =
					Long.parseLong (
						deliveryReportParam,
						16);

				if (statusCode == 0)
					newMessageStatus = MessageStatus.delivered;

				else if (statusCode >= 0x40 && statusCode <= 0x5f)
					newMessageStatus = MessageStatus.undelivered;

				// code = deliveryReportParam;

			} else if (submissionReportParam != null) {

				statusCode =
					Long.parseLong (
						submissionReportParam,
						16);

				if (statusCode == 0)
					newMessageStatus = MessageStatus.submitted;

				// code = submissionReportParam;

			} else {

				log.error (
					"Unrecognised report for " + messageId);

				return dialogueResponderProvider
					.get ();

			}

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					this);

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
				messageId,
				newMessageStatus,
				null,
				messageReportCode);

			transaction.commit ();

			return dialogueResponderProvider
				.get ();

		}

	};

	WebFile reportFile;

	Map<String,WebFile> routeFiles;

	@PostConstruct
	public
	void init () {

		reportFile =
			apiFile.get ()
				.postAction (reportAction);

		routeFiles =
			ImmutableMap.<String,WebFile>builder ()
				.put ("/in", inFile)
				.put ("/report", reportFile)
				.build ();

	}

	// ========================================================= servlet module

	final
	Entry inEntry =
		new Entry ("/in/(\\d+)") {

		@Override
		protected
		WebFile handle (
				Matcher matcher) {

			requestContext.request (
				"route_id",
				new Integer (
					matcher.group (1)));

			return inFile;
		}
	};

	final
	Entry routeEntry =
		new Entry ("/route/(\\d+)(/[^/]+)") {

		@Override
		protected
		WebFile handle (
				Matcher matcher) {

			requestContext.request (
				"route_id",
				Integer.parseInt (
					matcher.group (1)));

			return routeFiles.get (
				matcher.group (2));

		}

	};

	@Override
	public
	Map<String,PathHandler> paths () {

		return ImmutableMap.<String,PathHandler>builder ()

			.put ("/dialogue",
				new RegexpPathHandler (
					inEntry,
					routeEntry))

			.build ();

	}

	@Override
	public
	Map<String,WebFile> files () {
		return Collections.emptyMap ();
	}

}
