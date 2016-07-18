package wbs.integrations.dialogue.logic;

import static wbs.framework.utils.etc.Misc.equal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.integrations.dialogue.model.DialogueLocatorRec;
import wbs.sms.locator.logic.Locator;
import wbs.sms.locator.logic.LocatorException;
import wbs.sms.locator.model.LocatorObjectHelper;
import wbs.sms.locator.model.LocatorRec;
import wbs.sms.locator.model.LongLat;

@SingletonComponent ("dialogueLocator")
public
class DialogueLocator
	implements Locator {

	@Inject
	Database database;

	@Inject
	LocatorObjectHelper locatorHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	WbsConfig wbsConfig;

	@Override
	public
	List<String> getTypeCodes () {

		return Arrays.<String>asList (
			"dialogue_locator.default");

	}

	@Override
	public
	LongLat lookup (
			int locatorId,
			String number) {

		LocatorInfo locatorInfo =
			new LocatorInfo ();

		locatorInfo.number =
			number;

		lookupDialogueLocator (
			locatorInfo,
			locatorId);

		createContent (
			locatorInfo);

		openConnection (
			locatorInfo);

		sendRequest (
			locatorInfo);

		return readResponse (locatorInfo);

	}

	void lookupDialogueLocator (
			LocatorInfo locatorInfo,
			int locatorId) {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"DialogueLocator.lookupDialogueLocator (...)",
				this);

		LocatorRec locator =
			locatorHelper.findRequired (
				locatorId);

		DialogueLocatorRec dialogueLocator =
			(DialogueLocatorRec) (Object)
			objectManager.getParent (
				locator);

		locatorInfo.url =
			dialogueLocator.getUrl ();

		locatorInfo.account =
			dialogueLocator.getAccount ();

		locatorInfo.organiser =
			dialogueLocator.getOrganiser ();

		locatorInfo.password =
			dialogueLocator.getPassword ();

	}

	void openConnection (
			LocatorInfo locatorInfo) {

		try {

			try {

				locatorInfo.urlConn =
					(HttpURLConnection)
					new URL(locatorInfo.url).openConnection ();

			} catch (MalformedURLException exception) {

				throw new LocatorException (exception);

			}

			locatorInfo.urlConn.setDoOutput (
				true);

			locatorInfo.urlConn.setDoInput (
				true);

			locatorInfo.urlConn.setAllowUserInteraction (
				false);

			locatorInfo.urlConn.setRequestMethod (
				"POST");

			locatorInfo.urlConn.setRequestProperty (
				"User-Agent",
				wbsConfig.httpUserAgent ());

			locatorInfo.urlConn.setRequestProperty (
				"Content-Type",
				"text/xml");

			locatorInfo.urlConn.setRequestProperty (
				"Content-Length",
				Integer.toString (locatorInfo.content.length));

			locatorInfo.urlConn.setReadTimeout (
				180 * 1000);

		} catch (IOException exception) {

			throw new LocatorException (
				exception);

		}

	}

	void sendRequest (
			LocatorInfo locatorInfo) {

		try {

			OutputStream outputStream =
				locatorInfo.urlConn.getOutputStream ();

			outputStream.write (
				locatorInfo.content);

			outputStream.flush ();

		} catch (IOException exception) {

			throw new LocatorException (
				"Error sending request data",
				exception);

		}

	}

	void createContent (
			LocatorInfo locatorInfo)
		throws LocatorException {

		try {

			Element lbsElement =
				new Element ("LBS");

			Document document =
				new Document (lbsElement);

			Element requestElem =
				new Element ("Request");

			lbsElement.addContent (
				requestElem);

			Element authDataElem =
				new Element ("AuthenticationData")
					.setAttribute ("Account", locatorInfo.account)
					.setAttribute ("Organiser", locatorInfo.organiser)
					.setAttribute ("Password", locatorInfo.password);

			requestElem.addContent (
				authDataElem);

			Element locateElem =
				new Element ("Locate")
					.setAttribute ("format", "LL");

			requestElem.addContent (
				locateElem);

			Element msisdnElem =
				new Element ("MSISDN")
					.setAttribute ("network", "AUTO")
					.setText (locatorInfo.number);

			locateElem.addContent (
				msisdnElem);

			ByteArrayOutputStream byteArrayOutputStream =
				new ByteArrayOutputStream ();

			XMLOutputter xmlOutputter =
				new XMLOutputter (
					Format.getPrettyFormat ());

			xmlOutputter.output (
				document,
				byteArrayOutputStream);

			locatorInfo.content =
				byteArrayOutputStream.toByteArray ();

		} catch (IOException exception) {

			throw new LocatorException (
				"Error encoding request data",
				exception);

		}

	}

	LongLat readResponse (
			LocatorInfo locatorInfo) {

		try {

			SAXBuilder builder =
				new SAXBuilder ();

			Document document =
				builder.build (
					locatorInfo.urlConn.getInputStream ());

			Element rootElem =
				document.getRootElement ();

			if (! equal (rootElem.getName (), "LBS")) {

				throw new LocatorException (
					"Returned document was not an <LBS>");

			}

			Element responseElem =
				rootElem.getChild ("Response");

			if (responseElem == null) {

				throw new LocatorException (
					"No <Response> element in <LBS>");

			}

			Element errorElem =
				responseElem.getChild ("Error");

			if (errorElem != null) {

				throw new LocatorException (
					errorElem.getText ());

			}

			Element locateElem =
				responseElem.getChild ("Locate");

			if (locateElem == null) {

				throw new LocatorException (
					"No <Locate> element in <Response>");

			}

			Element locationElem =
				locateElem.getChild ("Location");

			if (locationElem == null) {

				throw new LocatorException (
					"No <Location> elemnt in <Locate>");

			}

			String latitudeString =
				locationElem.getAttributeValue ("latitude");

			if (latitudeString == null) {

				throw new LocatorException (
					"No latitude attribute in <Location>");

			}

			String longitudeString =
				locationElem.getAttributeValue ("longitude");

			if (longitudeString == null) {

				throw new LocatorException (
					"No longitude attribute in <Location>");

			}

			double longitude, latitude;

			try {

				longitude =
					Double.parseDouble (longitudeString);

			} catch (NumberFormatException exception) {

				throw new LocatorException (
					"Error parsing longitude value",
					exception);

			}

			try {

				latitude =
					Double.parseDouble (latitudeString);

			} catch (NumberFormatException exception) {

				throw new LocatorException (
					"Error parsing latitude value",
					exception);

			}

			return new LongLat (
				longitude,
				latitude);

		} catch (IOException exception) {

			throw new LocatorException (
				"Error reading result",
				exception);

		} catch (JDOMException exception) {

			throw new LocatorException (
				"Error parsing result",
				exception);

		}

	}

	static
	class LocatorInfo {

		String number;

		String url;
		String account;
		String organiser;
		String password;

		HttpURLConnection urlConn;

		byte[] content;

	}

}
