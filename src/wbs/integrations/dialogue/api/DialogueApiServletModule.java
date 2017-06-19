package wbs.integrations.dialogue.api;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringEqualSafe;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.servlet.ServletException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import wbs.api.mvc.ApiFile;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
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

import wbs.utils.io.RuntimeIoException;
import wbs.utils.string.FormatWriter;
import wbs.utils.string.WriterFormatWriter;

import wbs.web.context.RequestContext;
import wbs.web.file.AbstractWebFile;
import wbs.web.file.WebFile;
import wbs.web.mvc.WebAction;
import wbs.web.pathhandler.PathHandler;
import wbs.web.pathhandler.RegexpPathHandler;
import wbs.web.pathhandler.RegexpPathHandler.Entry;
import wbs.web.responder.WebModule;
import wbs.web.responder.WebResponder;

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

	@StrongPrototypeDependency
	ComponentProvider <ApiFile> apiFileProvider;

	@PrototypeDependency
	@NamedDependency ("dialogueResponder")
	ComponentProvider <WebResponder> dialogueResponderProvider;

	@PrototypeDependency
	ComponentProvider <RegexpPathHandler> regexpPathHandlerProvider;

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
				@NonNull TaskLogger parentTaskLogger) {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"inFile.doPost");

			) {

				// get request stuff

				Long routeId =
					requestContext.requestIntegerRequired (
						"route_id");

				// get params in local variables

				String numFromParam =
					requestContext.parameterRequired (
						"X-E3-Originating-Address");

				String numToParam =
					requestContext.parameterRequired (
						"X-E3-Recipients");

				String idParam =
					requestContext.parameterRequired (
						"X-E3-ID");

				String networkParam =
					requestContext.parameterRequired (
						"X-E3-Network");

				String hexMessageParam =
					requestContext.parameterRequired (
						"X-E3-Hex-Message");

				String userDataHeaderIndicatorParam =
					requestContext.parameterRequired (
						"X-E3-User-Data-Header-Indicator");

				String dataCodingSchemeParam =
					requestContext.parameterRequired (
						"X-E3-Data-Coding-Scheme");

				// decode the network

				Long networkId =
					networks.get (
						networkParam);

				// determine the character set from the data coding scheme

				int dataCodingScheme =
					Integer.parseInt (
						dataCodingSchemeParam,
						16);

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

					throw new RuntimeException (
						exception);

				}

				// strip off the user data header (if present)

				byte[] messageBytes;

				Optional <UserDataHeader> userDataHeader =
					optionalAbsent ();

				if (
					stringEqualSafe (
						userDataHeaderIndicatorParam,
						"1")
				) {

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
							optionalOf (
								UserDataHeader.decode (
									ByteBuffer.wrap (
										headerBytes)));

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

						throw new RuntimeException (
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

				saveMessage (
					taskLogger,
					routeId,
					optionalOf (
						networkId),
					numFromParam,
					numToParam,
					userDataHeader,
					message,
					idParam);

				try (

					Writer writer =
						requestContext.writer ();

					FormatWriter formatWriter =
						new WriterFormatWriter (
							writer);

				) {

					formatWriter.writeLineFormat (
						"<HTML>");

					formatWriter.writeLineFormat (
						"<!-- X-E3-Submission-Report: \"00\" -->");

					formatWriter.writeLineFormat (
						"</HTML>");

				} catch (IOException ioException) {

					throw new RuntimeIoException (
						ioException);

				}

			}

		}

		private
		void saveMessage (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Long routeId,
				@NonNull Optional <Long> networkIdOptional,
				@NonNull String numberFrom,
				@NonNull String numberTo,
				@NonNull Optional <UserDataHeader> userDataHeaderOptional,
				@NonNull String message,
				@NonNull String otherId) {

			try (

				OwnedTransaction transaction =
					database.beginReadWrite (
						logContext,
						parentTaskLogger,
						"saveMessage");

			) {

				RouteRec route =
					routeHelper.findRequired (
						transaction,
						routeId);

				Optional <NetworkRec> networkOptional =
					optionalMapRequired (
						networkIdOptional,
						networkId ->
							networkHelper.findRequired (
								transaction,
								networkId));

				// if it's a concatenated message...

				Optional <ConcatenatedInformationElement> concatOptional =
					optionalMapRequired (
						userDataHeaderOptional,
						nestedUserDataHeader ->
							nestedUserDataHeader.find (
								ConcatenatedInformationElement.class));

				if (
					optionalIsPresent (
						concatOptional)
				) {

					// insert a part message

					inboxMultipartLogic.insertInboxMultipart (
						transaction,
						route,
						concatOptional.get ().getRef (),
						concatOptional.get ().getSeqMax (),
						concatOptional.get ().getSeqNum (),
						numberTo,
						numberFrom,
						optionalAbsent (),
						networkOptional,
						optionalOf (
							otherId),
						message);

				} else {

					// insert a message

					smsInboxLogic.inboxInsert (
						transaction,
						optionalOf (
							otherId),
						textHelper.findOrCreate (
							transaction,
							message),
						smsNumberHelper.findOrCreate (
							transaction,
							numberFrom),
						numberTo,
						route,
						networkOptional,
						optionalAbsent (),
						emptyList (),
						optionalAbsent (),
						optionalAbsent ());

				}

				transaction.commit ();

			}

		}

	};

	private final
	ComponentProvider <WebAction> reportActionProvider =
		taskLogger ->
			new WebAction () {

		@Override
		public
		WebResponder handle (
				@NonNull TaskLogger parentTaskLogger) {

			try (

				OwnedTransaction transaction =
					database.beginReadWrite (
						logContext,
						parentTaskLogger,
						"handle");

			) {

				String idParam =
					requestContext.parameterRequired (
						"X-E3-ID");

				String deliveryReportParam =
					requestContext.parameterRequired (
						"X-E3-Delivery-Report");

				String submissionReportParam =
					requestContext.parameterRequired (
						"X-E3-Submission-Report");

				String userKeyParam =
					requestContext.parameterRequired (
						"X-E3-User-Key");

				// TODO dump params

				for (
					Map.Entry<String,List<String>> entry
						: requestContext.parameterMap ().entrySet ()
				) {

					String name =
						entry.getKey ();

					List<String> values =
						entry.getValue ();

					for (
						String value
							: values
					) {

						transaction.debugFormat (
							"Param %s = %s",
							name,
							value);

					}

				}

				// check for a user key

				if (userKeyParam == null) {

					transaction.warningFormat (
						"Ignoring dialogue report with no user key, X-E3-ID=%s",
						idParam);

					return dialogueResponderProvider.provide (
						transaction);

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

					transaction.errorFormat (
						"Unrecognised report for %s",
						integerToDecimalString (
							messageId));

					return dialogueResponderProvider.provide (
						transaction);

				}

				reportLogic.deliveryReport (
					transaction,
					messageId,
					newMessageStatus,
					optionalOf (
						deliveryReportParam),
					optionalAbsent (),
					optionalAbsent (),
					optionalAbsent ());

				transaction.commit ();

				return dialogueResponderProvider.provide (
					transaction);

			}

		}

	};

	WebFile reportFile;

	Map <String, WebFile> routeFiles;

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"init");

		) {

			reportFile =
				apiFileProvider.provide (
					taskLogger)

				.postActionProvider (
					reportActionProvider);

			routeFiles =
				ImmutableMap.<String,WebFile>builder ()
					.put ("/in", inFile)
					.put ("/report", reportFile)
					.build ();

		}

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
	Map <String, PathHandler> webModulePaths (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"paths");

		) {

			return ImmutableMap.<String, PathHandler> builder ()

				.put (
					"/dialogue",
					regexpPathHandlerProvider.provide (
						taskLogger)

					.add (
						inEntry)

					.add (
						routeEntry)

				)

				.build ()

			;

		}

	}

}
