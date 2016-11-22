package wbs.integrations.mediaburst.api;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.BinaryUtils.bytesFromHex;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.lowercase;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringIsNotEmpty;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.servlet.ServletException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.web.context.RequestContext;
import wbs.web.file.AbstractWebFile;
import wbs.web.file.WebFile;
import wbs.web.pathhandler.PathHandler;
import wbs.web.pathhandler.RegexpPathHandler;
import wbs.web.responder.WebModule;

@Log4j
@SingletonComponent ("meidaburtApiServletModule")
public
class MediaburstApiServletModule
	implements WebModule {

	// singleton dependencies

	@SingletonDependency
	Database database;

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
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	NumberObjectHelper smsNumberHelper;

	@SingletonDependency
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
		void doPost (
				@NonNull TaskLogger parentTaskLogger)
			throws
				ServletException,
				IOException {

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"inFile.doPost");

			// logger.info("MB: "+requestContext.getRequest().getQueryString());

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					"MediaburstApiServletModule.inFile.doPost ()",
					this);

			// debugging

			requestContext.debugParameters (
				taskLogger);

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
							bytesFromHex (
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
					optionalOf (
						msgIdParam),
					messageText,
					smsNumberHelper.findOrCreate (
						numFromParam),
					numToParam,
					route,
					optionalFromNullable (
						network),
					optionalAbsent (),
					emptyList (),
					optionalAbsent (),
					optionalAbsent ());

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
		void doGet (
				@NonNull TaskLogger parentTaskLogger)
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
						optionalOf (
							statusParam),
						optionalAbsent (),
						optionalOf (
							joinWithSpace (
								stringFormat (
									"status=%s",
									statusParam),
								stringFormat (
									"deliver_code=%s",
									deliverCodeParam))),
						optionalAbsent ());

				}

				transaction.commit ();

			} catch (NoSuchMessageException exception) {

				log.fatal (
					stringFormat (
						"Ignoring report for unknown message %s/%s",
						integerToDecimalString (
							requestContext.requestIntegerRequired (
								"routeId")),
						requestContext.parameterOrNull (
							"msg_id")));

			} catch (InvalidMessageStateException exception) {

				log.fatal (
					stringFormat (
						"Ignoring report for message %s/%s: %s",
						integerToDecimalString (
							requestContext.requestIntegerRequired (
								"routeId")),
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
				parseIntegerRequired (
					matcher.group (1)));

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
