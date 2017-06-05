package wbs.integrations.mediaburst.api;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.BinaryUtils.bytesFromHex;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.lowercase;
import static wbs.utils.string.StringUtils.stringFormat;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
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

import wbs.utils.string.FormatWriter;

import wbs.web.context.RequestContext;
import wbs.web.file.AbstractWebFile;
import wbs.web.file.WebFile;
import wbs.web.pathhandler.PathHandler;
import wbs.web.pathhandler.RegexpPathHandler;
import wbs.web.responder.WebModule;

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

	// prototype dependencies

	@PrototypeDependency
	Provider <RegexpPathHandler> regexpPathHandlerProvider;

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
				@NonNull TaskLogger parentTaskLogger) {

			try (

				OwnedTransaction transaction =
					database.beginReadWriteWithoutParameters (
						logContext,
						parentTaskLogger,
						"inFile.doPost");

			) {

				// debugging

				requestContext.debugParameters (
					transaction);

				// get request stuff

				Long routeId =
					requestContext.requestIntegerRequired (
						"routeId");

				// get params in local variables

				String numFromParam =
					requestContext.parameterRequired (
						"phonenumber");

				String numToParam =
					requestContext.parameterRequired (
						"tonumber");

				Optional <String> networkParamOptional =
					requestContext.parameter (
						"netid");

				String messageParam =
					requestContext.parameterRequired (
						"message");

				String msgIdParam =
					requestContext.parameterRequired (
						"msg_id");

				Optional <String> udhParamOptional =
					requestContext.parameter (
						"udh");

				// lookup objects

				RouteRec route =
					routeHelper.findRequired (
						transaction,
						routeId);

				Optional <NetworkRec> networkOptional =
					optionalMapRequired (
						networkParamOptional,
						networkParam ->
							networkHelper.findRequired (
								transaction,
								networkMap.get (
									networkParam)));

				// check for concatenation

				UserDataHeader userDataHeader;

				if (
					optionalIsPresent (
						udhParamOptional)
				) {

					String udhParam =
						optionalGetRequired (
							udhParamOptional);

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
					textHelper.findOrCreate (
						transaction,
						messageParam);

				if (concatenatedInformationElement != null) {

					inboxMultipartLogic.insertInboxMultipart (
						transaction,
						route,
						concatenatedInformationElement.getRef (),
						concatenatedInformationElement.getSeqMax (),
						concatenatedInformationElement.getSeqNum (),
						numToParam,
						numFromParam,
						optionalAbsent (),
						networkOptional,
						optionalOf (
							msgIdParam),
						messageText.getText ());

				} else {

					smsInboxLogic.inboxInsert (
						transaction,
						optionalOf (
							msgIdParam),
						messageText,
						smsNumberHelper.findOrCreate (
							transaction,
							numFromParam),
						numToParam,
						route,
						networkOptional,
						optionalAbsent (),
						emptyList (),
						optionalAbsent (),
						optionalAbsent ());

				}

				// commit

				transaction.commit ();

			}

			// send response

			try (

				FormatWriter formatWriter =
					requestContext.formatWriter ();

			) {

				formatWriter.writeLineFormat (
					"OK");

			}

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
				@NonNull TaskLogger parentTaskLogger) {

			try (

				OwnedTransaction transaction =
					database.beginReadWriteWithoutParameters (
						logContext,
						parentTaskLogger,
						"reportFile.doGet");

			) {

				RouteRec route =
					routeHelper.findRequired (
						transaction,
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
							transaction,
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

					throw transaction.fatalFormat (
						"Ignoring report for unknown message %s/%s",
						integerToDecimalString (
							requestContext.requestIntegerRequired (
								"routeId")),
						requestContext.parameterRequired (
							"msg_id"));

				} catch (InvalidMessageStateException exception) {

					throw transaction.fatalFormat (
						"Ignoring report for message %s/%s: %s",
						integerToDecimalString (
							requestContext.requestIntegerRequired (
								"routeId")),
						requestContext.parameterRequired (
							"msg_id"),
						exception.getMessage ());

				}

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
	Map <String, PathHandler> paths () {

		return ImmutableMap.<String, PathHandler> builder ()

			.put (
				"/mediaburst",
				regexpPathHandlerProvider.get ()

				.add (
					routeEntry)

			)

			.build ()

		;

	}

	@Override
	public
	Map<String,WebFile> files () {
		return null;
	}

	// data

	public final static
	Map <String, Long> networkMap =
		ImmutableMap.<String, Long> builder ()

		.put ("51", 1l)
		.put ("81", 2l)
		.put ("3", 3l)
		.put ("1", 4l)
		.put ("9", 6l)

		.build ();

}
