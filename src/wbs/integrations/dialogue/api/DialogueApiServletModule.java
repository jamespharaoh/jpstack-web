package wbs.integrations.dialogue.api;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Provider;
import javax.servlet.ServletException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import lombok.NonNull;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import wbs.api.mvc.ApiFile;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.text.model.TextObjectHelper;

import wbs.sms.gsm.ConcatenatedInformationElement;
import wbs.sms.gsm.UserDataHeader;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.logic.SmsInboxMultipartLogic;
import wbs.sms.message.report.logic.SmsDeliveryReportLogic;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.file.AbstractWebFile;
import wbs.web.file.WebFile;
import wbs.web.pathhandler.PathHandler;
import wbs.web.pathhandler.RegexpPathHandler;
import wbs.web.pathhandler.RegexpPathHandler.Entry;
import wbs.web.responder.Responder;
import wbs.web.responder.WebModule;

@SingletonComponent ("dialogueApiServletModule")
public
class DialogueApiServletModule
	implements WebModule {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	SmsInboxMultipartLogic inboxMultipartLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NetworkObjectHelper networkHelper;

	@SingletonDependency
	SmsDeliveryReportLogic reportLogic;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	NumberObjectHelper smsNumberHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <ApiFile> apiFileProvider;

	@PrototypeDependency
	Provider <DialogueResponder> dialogueResponderProvider;

	// =============================================================== networks

	// TODO this belongs in the database

	private final static
	Map <String, Long> networks =
		ImmutableMap.<String, Long> builder ()
			.put ("Orange", 1l)
			.put ("Vodafone", 2l)
			.put ("One2One", 3l)
			.put ("Cellnet", 4l)
			.put ("Virgin", 5l)
			.put ("ThreeUK", 6l)
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
	private
	WebFile inFile =
		new AbstractWebFile () {

		@Override
		public
		void doPost (
				@NonNull TaskLogger parentTaskLogger)
			throws
				ServletException,
				IOException {

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"inFile.doPost");

			// get request stuff

			Long routeId =
				requestContext.requestIntegerRequired (
					"route_id");

			// get params in local variables

			String numFromParam =
				requestContext.parameterOrNull ("X-E3-Originating-Address");

			String numToParam =
				requestContext.parameterOrNull ("X-E3-Recipients");

			String idParam =
				requestContext.parameterOrNull ("X-E3-ID");

			String networkParam =
				requestContext.parameterOrNull ("X-E3-Network");

			String hexMessageParam =
				requestContext.parameterOrNull ("X-E3-Hex-Message");

			String userDataHeaderIndicatorParam =
				requestContext.parameterOrNull ("X-E3-User-Data-Header-Indicator");

			String dataCodingSchemeParam =
				requestContext.parameterOrNull ("X-E3-Data-Coding-Scheme");

			// decode the network

			Long networkId =
				networks.get (
					networkParam);

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

					taskLogger.debugFormat (
						"udh.length = %s",
						integerToDecimalString (
							headerBytes.length));

					for (int i = 0; i < headerBytes.length; i++) {

						taskLogger.debugFormat (
							"udh [%s] = %s",
							integerToDecimalString (
								i),
							integerToDecimalString (
								headerBytes [i] & 0xff));

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

				} catch (Exception exception) {

					taskLogger.debugFormat (
						"Error decoding user data header: %s",
						exception.getMessage ());

					requestContext.debugParameters (
						taskLogger);

					throw new ServletException (
						exception);

				}

			} else {

				messageBytes = allBytes;

			}

			// decode the message body

			String message;

			try {

				message =
					new String (
						messageBytes,
						charset);

			} catch (UnsupportedEncodingException exception) {

				throw new RuntimeException (
					exception);

			}

			// save the message

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					"DialogueApiServletModule.inFile.doPost ()",
					this);

			RouteRec route =
				routeHelper.findRequired (
					routeId);

			NetworkRec network =
				networkId == null
					? null
					: networkHelper.findRequired (
						networkId);

			// if it's a concatenated message...

			ConcatenatedInformationElement concat =
				userDataHeader != null
					? userDataHeader.find (
						ConcatenatedInformationElement.class)
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

				smsInboxLogic.inboxInsert (
					optionalOf (
						idParam),
					textHelper.findOrCreate (
						message),
					smsNumberHelper.findOrCreate (
						numFromParam),
					numToParam,
					route,
					optionalOf (
						network),
					optionalAbsent (),
					emptyList (),
					optionalAbsent (),
					optionalAbsent ());

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
	Provider <Action> reportActionProvider =
		() -> new Action () {

		@Override
		public
		Responder handle (
				@NonNull TaskLogger parentTaskLogger) {

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"reportAction.handle");

			String idParam =
				requestContext.parameterOrNull ("X-E3-ID");

			String deliveryReportParam =
				requestContext.parameterOrNull ("X-E3-Delivery-Report");

			String submissionReportParam =
				requestContext.parameterOrNull ("X-E3-Submission-Report");

			String userKeyParam =
				requestContext.parameterOrNull ("X-E3-User-Key");

			// [TEMP] dump params

			for (Map.Entry<String,List<String>> entry
					: requestContext.parameterMap ().entrySet ()) {

				String name =
					entry.getKey ();

				List<String> values =
					entry.getValue ();

				for (
					String value
						: values
				) {

					taskLogger.debugFormat (
						"Param %s = %s",
						name,
						value);

				}

			}

			// check for a user key

			if (userKeyParam == null) {

				taskLogger.warningFormat (
					"Ignoring dialogue report with no user key, X-E3-ID=%s",
					idParam);

				return dialogueResponderProvider
					.get ();

			}

			Long messageId =
				Long.parseLong (
					userKeyParam);

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

				taskLogger.errorFormat (
					"Unrecognised report for %s",
					integerToDecimalString (
						messageId));

				return dialogueResponderProvider
					.get ();

			}

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					"DialogueApiServletModule.reportAction.handle ()",
					this);

			reportLogic.deliveryReport (
				messageId,
				newMessageStatus,
				Optional.of (
					deliveryReportParam),
				Optional.absent (),
				Optional.absent (),
				Optional.absent ());

			transaction.commit ();

			return dialogueResponderProvider
				.get ();

		}

	};

	WebFile reportFile;

	Map <String, WebFile> routeFiles;

	@NormalLifecycleSetup
	public
	void init () {

		reportFile =
			apiFileProvider.get ()

			.postActionProvider (
				reportActionProvider);

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
