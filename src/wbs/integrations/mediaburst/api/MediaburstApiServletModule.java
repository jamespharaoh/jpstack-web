package wbs.integrations.mediaburst.api;

import static wbs.framework.utils.etc.Misc.fromHex;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.StringUtils.joinWithSpace;
import static wbs.framework.utils.etc.StringUtils.lowercase;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringIsNotEmpty;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.servlet.ServletException;

import org.joda.time.Instant;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.AbstractWebFile;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RegexpPathHandler;
import wbs.framework.web.RequestContext;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.sms.core.logic.NoSuchMessageException;
import wbs.sms.gsm.ConcatenatedInformationElement;
import wbs.sms.gsm.UserDataHeader;
import wbs.sms.message.core.logic.InvalidMessageStateException;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.logic.SmsInboxMultipartLogic;
import wbs.sms.message.report.logic.SmsDeliveryReportLogic;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@Log4j
@SingletonComponent ("meidaburtApiServletModule")
public
class MediaburstApiServletModule
	implements ServletModule {

	// dependencies

	@Inject
	Database database;

	@Inject
	SmsInboxLogic smsInboxLogic;

	@Inject
	SmsInboxMultipartLogic inboxMultipartLogic;

	@Inject
	NetworkObjectHelper networkHelper;

	@Inject
	SmsDeliveryReportLogic reportLogic;

	@Inject
	RequestContext requestContext;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	TextObjectHelper textHelper;

	// implementation

	public static
	String hexToUnicode (
			String hexString) {

		StringBuilder output =
			new StringBuilder ();

		String subString = null;

		for (
			int postition = 0;
			postition < hexString.length ();
			postition = postition + 2
		) {

			subString =
				hexString.substring (
					postition,
					postition + 2);

			char character =
				(char)
				Integer.parseInt (
					subString,
					16);

			output.append (
				character);

		}

		return output.toString ();

	}

	/*
	String testContent (
			@NonNull String messageParam) {

		try {

			byte[] bytes =
				messageParam.getBytes ();

			boolean zeroBytes = false;

			for (int i = 0; i < bytes.length; i ++) {

				if (bytes [i] != 0)
					continue;

				zeroBytes = true;

				break;

			}

			if (! zeroBytes)
				return messageParam;

			// convert

			StringBuilder stringBuilder =
				new StringBuilder ();

			for (int i = 0; i < bytes.length; i ++) {

				if (bytes [i] != 0) {

					stringBuilder.append (
						Integer.toHexString (bytes [i]));

				}

			}

			return hexToUnicode (
				stringBuilder.toString ());

		} catch (Exception exception) {

			throw new RuntimeException (
				exception);

		}

	}
	*/

	/** WebFile to handle incoming messages. */

	WebFile inFile =
		new AbstractWebFile () {

		@Override
		public
		void doPost ()
			throws
				ServletException,
				IOException {

			// logger.info("MB: "+requestContext.getRequest().getQueryString());

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					"MediaburstApiServletModule.inFile.doPost ()",
					this);

			// debugging

			requestContext.debugParameters (
				log);

			// get request stuff

			Long routeId =
				requestContext.requestIntegerRequired (
					"routeId");

			// get params in local variables

			String numFromParam =
				requestContext.parameterOrNull ("phonenumber");

			String numToParam =
				requestContext.parameterOrNull ("tonumber");

			String networkParam =
				requestContext.parameterOrNull ("netid");

			String messageParam =
				requestContext.parameterOrNull ("message");

			String msgIdParam =
				requestContext.parameterOrNull ("msg_id");

			String udhParam =
				requestContext.parameterOrNull ("udh");

			Long networkId = null;

			if (networkParam != null) {
				if (networkParam.equals("51"))
					networkId = 1l;
				else if (networkParam.equals("81"))
					networkId = 2l;
				else if (networkParam.equals("3"))
					networkId = 3l;
				else if (networkParam.equals("1"))
					networkId = 4l;
				else if (networkParam.equals("9"))
					networkId = 6l;
				// else throw new RuntimeException ("Unknown network: " +
				// networkParam);
			}

			/*
			messageParam =
				testContent (
					messageParam);
			*/

			// load the stuff

			RouteRec route =
				routeHelper.findRequired (
					routeId);

			NetworkRec network =
				networkId == null
					? null
					: networkHelper.findRequired (
						networkId);

			// check for concatenation

			UserDataHeader userDataHeader;

			if (
				stringIsNotEmpty (
					udhParam)
			) {

				userDataHeader =
					UserDataHeader.decode (
						ByteBuffer.wrap (
							fromHex (
								udhParam)));

			} else {

				userDataHeader =
					null;

			}

			ConcatenatedInformationElement concatenatedInformationElement;

			if (
				userDataHeader != null
			) {

				concatenatedInformationElement =
					userDataHeader.find (
						ConcatenatedInformationElement.class);

			} else {

				concatenatedInformationElement =
					null;

			}

			// insert the message

			TextRec messageText =
				textHelper.findOrCreate (messageParam);

			if (concatenatedInformationElement != null) {

				inboxMultipartLogic.insertInboxMultipart (
					route,
					concatenatedInformationElement.getRef (),
					concatenatedInformationElement.getSeqMax (),
					concatenatedInformationElement.getSeqNum (),
					numToParam,
					numFromParam,
					null,
					network,
					msgIdParam,
					messageText.getText ());

			} else {

				smsInboxLogic.inboxInsert (
					Optional.of (msgIdParam),
					messageText,
					numFromParam,
					numToParam,
					route,
					Optional.fromNullable (network),
					Optional.<Instant>absent (),
					Collections.<MediaRec>emptyList (),
					Optional.<String>absent (),
					Optional.<String>absent ());

			}

			// commit etc

			transaction.commit ();

			PrintWriter out =
				requestContext.writer ();

			out.println ("OK");

		}

	};

	static
	Map<String,MessageStatus> stringToMessageStatus =
		ImmutableMap.<String,MessageStatus>builder ()
			.put ("acceptd", MessageStatus.submitted)
			.put ("deleted", MessageStatus.undelivered)
			.put ("delivrd", MessageStatus.delivered)
			.put ("enroute", MessageStatus.submitted)
			.put ("expired", MessageStatus.undelivered)
			.put ("rejectd", MessageStatus.undelivered)
			.put ("undeliv", MessageStatus.undelivered)
			.build ();

	static Set<String> messageStatusStrings =
		ImmutableSet.<String>builder ()
			.addAll (stringToMessageStatus.keySet ())
			.add ("unknown")
			.build ();

	WebFile reportFile =
		new AbstractWebFile () {

		@Override
		public
		void doGet ()
			throws
				ServletException,
				IOException {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					"MediaburstApiServletModule.reportFile.doGet ()",
					this);

			RouteRec route =
				routeHelper.findRequired (
					requestContext.requestIntegerRequired (
						"routeId"));

			String statusParam =
				lowercase (
					requestContext.parameterRequired (
						"status"));

			if (! messageStatusStrings.contains (statusParam)) {

				throw new RuntimeException (
					"Unknown message status: " + statusParam);

			}

			MessageStatus newMessageStatus =
				stringToMessageStatus.get (
					statusParam.toLowerCase ());

			String deliverCodeParam =
				requestContext.parameterOrDefault (
					"deliver_code",
					"");

			try {

				if (
					isNotNull (
						newMessageStatus)
				) {

					reportLogic.deliveryReport (
						route,
						requestContext.parameterRequired (
							"msg_id"),
						newMessageStatus,
						Optional.of (
							statusParam),
						Optional.absent (),
						Optional.of (
							joinWithSpace (
								stringFormat (
									"status=%s",
									statusParam),
								stringFormat (
									"deliver_code=%s",
									deliverCodeParam))),
						Optional.absent ());

				}

				transaction.commit ();

			} catch (NoSuchMessageException exception) {

				log.fatal (
					stringFormat (
						"Ignoring report for unknown message %s/%s",
						requestContext.requestInteger (
							"routeId"),
						requestContext.parameterOrNull (
							"msg_id")));

			} catch (InvalidMessageStateException exception) {

				log.fatal (
					stringFormat (
						"Ignoring report for message %s/%s: %s",
						requestContext.requestInteger (
							"routeId"),
						requestContext.parameterOrNull (
							"msg_id"),
						exception.getMessage ()));

			}

		}

	};

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

	// ============================================================ path handler

	final
	PathHandler pathHandler =
		new RegexpPathHandler (routeEntry);

	// ============================================================ files

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
			.put ("/mediaburst", pathHandler)
			.build ();

	}

	@Override
	public
	Map<String,WebFile> files () {
		return null;
	}

}
