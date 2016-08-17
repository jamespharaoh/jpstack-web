package wbs.integrations.mediaburst.daemon;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.object.ObjectManager;
import wbs.framework.utils.etc.Xom;
import wbs.integrations.mediaburst.model.MediaburstNetworkObjectHelper;
import wbs.integrations.mediaburst.model.MediaburstNetworkRec;
import wbs.integrations.mediaburst.model.MediaburstProteusRouteOutObjectHelper;
import wbs.integrations.mediaburst.model.MediaburstProteusRouteOutRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.AbstractSmsSender1;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.message.wap.model.WapPushMessageObjectHelper;
import wbs.sms.message.wap.model.WapPushMessageRec;
import wbs.sms.network.logic.NetworkPrefixCache;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteRec;

/**
 * Daemon service to process outbox items for mediaburst proteus routes.
 */
@Log4j
@SingletonComponent ("mediaburstProteusSender")
public
class MediaburstProteusSender
	extends AbstractSmsSender1<MediaburstProteusSender.State> {

	@Inject
	MediaburstNetworkObjectHelper mediaburstNetworkHelper;

	@Inject
	MediaburstProteusRouteOutObjectHelper mediaburstProteusRouteOutHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	NetworkPrefixCache networkPrefixCache;

	@Inject
	WapPushMessageObjectHelper wapPushMessageHelper;

	@Inject
	WbsConfig wbsConfig;

	@Override
	protected
	String getThreadName () {
		return "MbPrSndr";
	}

	@Override
	protected
	String getSenderCode () {
		return "mediaburst_proteus";
	}

	@Override
	protected
	State getMessage (
			OutboxRec outbox) {

		State proteusOutbox =
			new State ();

		// get stuff

		proteusOutbox.messageId = outbox.getId ();
		proteusOutbox.message = outbox.getMessage ();
		proteusOutbox.route = proteusOutbox.message.getRoute ();

		// lookup proteus route

		proteusOutbox.proteusRouteOut =
			mediaburstProteusRouteOutHelper.findOrThrow (
				proteusOutbox.route.getId (),
				() -> tempFailure (
					stringFormat (
						"Proteus outbound route not found for %s",
						proteusOutbox.route.getCode ())));

		if (proteusOutbox.route.getOutCharge() > 0) {

			// lookup network

			NetworkRec network =
				proteusOutbox.message.getNumber ().getNetwork ();

			if (network == null) {

				network =
					networkPrefixCache.lookupNetwork (
						proteusOutbox.message.getNumber ().getNumber ());

			}

			if (network == null) {

				throw permFailure (
					"Don't know network for premium message " + proteusOutbox.messageId);

			}

			// lookup mediaburst network

			proteusOutbox.mediaburstNetwork =
				mediaburstNetworkHelper.findOrThrow (
					network.getId (),
					() -> permFailure (
						stringFormat (
							"Cannot find Mediaburst network information for message %s",
							proteusOutbox.messageId)));

		}

		// load lazy stuff

		proteusOutbox.message.getText ().getText ();

		proteusOutbox.servicePath =
			objectManager.objectPathMini (
				proteusOutbox.message.getService ());

		// pick a handler

		if (
			equal (
				proteusOutbox.message.getMessageType ().getCode (),
				"sms")
		) {

			// nothing to do

		} else if (
			equal (
				proteusOutbox.message.getMessageType ().getCode (),
				"wap_push")
		) {

			// load wap push stuff

			proteusOutbox.wapPushMessage =
				wapPushMessageHelper.findOrThrow (
					outbox.getId (),
					() -> tempFailure (
						stringFormat (
							"Wap push message not found for message %s",
							outbox.getId ())));

			proteusOutbox.wapPushMessage.getUrlText ().getText ();

			proteusOutbox.wapPushMessage.getTextText ().getText ();

		} else {

			// unrecognised message type

			throw tempFailure (
				stringFormat (
					"Don't know what to do with a %s",
					proteusOutbox.message.getMessageType ().getCode ()));
		}

		return proteusOutbox;

	}

	@Override
	protected
	Optional<List<String>> sendMessage (
			@NonNull State proteusOutbox) {

		log.info (
			"Sending message " + proteusOutbox.messageId);

		try {

			// open the connection

			HttpURLConnection urlConnection =
				openConnection (
					proteusOutbox);

			// send http request

			sendRequest (
				proteusOutbox,
				urlConnection.getOutputStream ());

			// and interpret the response

			return Optional.of (
				ImmutableList.of (
					readResponse (
						urlConnection.getInputStream ())));

		} catch (IOException exception) {

			throw tempFailure (
				"IO error " + exception.getMessage ());

		}

	}

	/**
	 * Class to hold all the DB information we need while sending a message.
	 */
	public static
	class State {
		Long messageId;
		OutboxRec outbox;
		MessageRec message;
		RouteRec route;
		MediaburstProteusRouteOutRec proteusRouteOut;
		MediaburstNetworkRec mediaburstNetwork;
		WapPushMessageRec wapPushMessage;
		String servicePath;
	}

	/**
	 * Opens the connection, setting all appropriate parameters.
	 */
	HttpURLConnection openConnection (
			State proteusOutbox)
		throws IOException {

		// create connection

		URL url =
			new URL (
				proteusOutbox.proteusRouteOut.getUrl ());

		HttpURLConnection urlConnection =
			(HttpURLConnection)
			url.openConnection ();

		// set basic params

		urlConnection.setDoInput (
			true);

		urlConnection.setDoOutput (
			true);

		urlConnection.setAllowUserInteraction (
			false);

		urlConnection.setRequestMethod (
			"POST");

		// set request params

		urlConnection.setRequestProperty (
			"User-Agent",
			wbsConfig.httpUserAgent ());

		urlConnection.setRequestProperty (
			"Content-Type",
			"text/xml");

		// and return
		return urlConnection;
	}

	public
	void sendRequest (
			State state,
			OutputStream outputStream)
		throws IOException {

		Document document =
			createXml (
				state);

		Serializer ser =
			new Serializer (
				outputStream);

		ser.write (
			document);

	}

	public
	Document createXml (
			State state)
		throws IOException {

		// do the standard stuff

		Element smsElement;

		Document document =
			new Document (
				Xom.xomElem (
					"Message",

					Xom.xomElem (
						"Username",
						state.proteusRouteOut.getUsername ()),

					Xom.xomElem (
						"Password",
						state.proteusRouteOut.getPassword ()),

					smsElement = Xom.xomElem (
						"SMS",

						Xom.xomElem (
							"To",
							state.message.getNumTo ()),

						Xom.xomElem (
							"From",
							state.message.getNumFrom ()),

						Xom.xomElem (
							"ClientID",
							state.message.getId ().toString ()),

						Xom.xomElem (
							"ExpiryTime",
							"1440"),

						Xom.xomElem (
							"DlrType",
							"3"),

						Xom.xomElem (
							"DlrUrl",
							stringFormat (
								"%s",
								wbsConfig.apiUrl (),
								"/mediaburst",
								"/proteus",
								"/route",
								"/%u",
								state.route.getId (),
								"/report")),

						Xom.xomElem (
							"DlrContent",
							createDlrContent ()))));

		// add serv type where appropriate

		if (state.proteusRouteOut.getServType () != null) {

			Xom.xomAppend (
				smsElement,

				Xom.xomElem (
					"ServType",
					state.proteusRouteOut.getServType ()));

		}

		// add billed elements where appropriate

		if (state.route.getOutCharge () > 0) {

			Xom.xomAppend (
				smsElement,

				Xom.xomElem (
					"Billed",
					state.route.getOutCharge ().toString ()),

				Xom.xomElem (
					"Keyword",
					state.servicePath),

				Xom.xomElem (
					"NetworkID",
					state.mediaburstNetwork.getOtherId ().toString ()));

		}

		// add normal sms stuff where appropriate

		if (
			equal (
				state.message.getMessageType ().getCode (),
				"sms")
		) {

			Xom.xomAppend (
				smsElement,

				Xom.xomElem (
					"MsgType",
					"TEXT"),

				Xom.xomElem (
					"Content",
					state.message.getText ().getText ()));

		}

		// add wap push stuff where appropriate

		if (
			equal (
				state.message.getMessageType ().getCode (),
				"wap_push")
		) {

			Xom.xomAppend (
				smsElement,

				Xom.xomElem (
					"MsgType",
					"WAP_BOOKMARK"),

				Xom.xomElem (
					"Content",
					state.wapPushMessage.getTextText ().getText ()),

				Xom.xomElem (
					"URL",
					state.wapPushMessage.getUrlText ().getText ()));

		}

		return document;
	}

	public
	String createDlrContent ()
		throws IOException {

		// create the document

		Document document =

			new Document (
				Xom.xomElem (
					"DeliveryReceipt",

					Xom.xomElem (
						"ClientID",
						"#CLIENT_ID#"),

					Xom.xomElem (
						"Status",
						"#DELIVERY_STATUS#"),

					Xom.xomElem (
						"Dest",
						"#DEST_ADDR#"),

					Xom.xomElem (
						"ErrCode",
						"#ERR_CODE#"),

					Xom.xomElem (
						"MsgID",
						"#MSG_ID#"),

					Xom.xomElem (
						"Source",
						"#SRC_ADDR#")));

		// turn it into a string

		return document.toXML ();

	}

	public
	String readResponse (
			InputStream in)
		throws
			IOException,
			SendFailureException {

		try {

			Builder builder =
				new Builder ();

			Document document =
				builder.build (in);

			log.debug (
				"Got: " + document.toXML ());

			return readXml (
				document);

		} catch (ParsingException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public
	String readXml (
			Document document)
		throws SendFailureException {

		// check for main error

		Nodes nodes =
			document.query (
				"/Message_Resp/ErrNo");

		if (nodes.size () > 0) {

			int errNo =
				Integer.parseInt (
					nodes.get (0).getValue ());

			String errDesc =
				document.query (
					"/Message_Resp/ErrDesc"
				).get (0).getValue ();

			throw tempFailure (
				"Got general error: " + errNo + "; " + errDesc);

		}

		// check for sms error

		nodes =
			document.query (
				"/Message_Resp/SMS_Resp/ErrNo");

		if (nodes.size () > 0) {

			int errNo =
				Integer.parseInt (
					nodes.get (0).getValue ());

			String errDesc =
				document.query (
					"/Message_Resp/SMS_Resp/ErrDesc"
				).get (0).getValue ();

			if (
				in (
					errNo,
					10,
					11)
			) {

				throw permFailure (
					"Got message error: " + errNo + "; " + errDesc);

			} else {

				throw tempFailure (
					"Got message error: " + errNo + "; " + errDesc);

			}

		}

		// retrieve message id

		Element messageIdElem =
			(Element)
			document.query (
				"/Message_Resp/SMS_Resp/MessageID"
			).get (0);

		return messageIdElem.getValue ();

	}

}
