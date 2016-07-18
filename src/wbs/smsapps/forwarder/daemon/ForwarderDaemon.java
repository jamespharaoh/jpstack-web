package wbs.smsapps.forwarder.daemon;

import static wbs.framework.utils.etc.Misc.earlierThan;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.platform.daemon.QueueBuffer;
import wbs.smsapps.forwarder.model.ForwarderMessageInObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec;

@Log4j
@SingletonComponent ("forwarderDaemon")
public
class ForwarderDaemon
	extends AbstractDaemonService {

	// constants

	public final static
	int bufferSize = 100;

	// dependencies

	@Inject
	Database database;

	@Inject
	ForwarderMessageInObjectHelper forwarderMessageInHelper;

	@Inject
	WbsConfig wbsConfig;

	// state

	QueueBuffer<Integer,Integer> buffer =
		new QueueBuffer<Integer,Integer> (
			bufferSize);

	private
	class MainThread
		implements Runnable {

		public
		boolean doQuery () {

			Set<Integer> activeIds =
				buffer.getKeys ();

			List<ForwarderMessageInRec> forwarderMessageIns;

			@Cleanup
			Transaction transaction =
				database.beginReadOnly (
					"ForwarderDaemon.MainThread.doQuery ()",
					this);

			// get the list

			forwarderMessageIns =
				forwarderMessageInHelper.findNextLimit (
					transaction.now (),
					buffer.getFullSize ());

			// initialise any proxies

			for (
				ForwarderMessageInRec forwarderMessageIn
					: forwarderMessageIns
			) {

				forwarderMessageIn
					.getMessage ()
					.getText ()
					.getText ();

				forwarderMessageIn
					.getForwarder ()
					.getUrl ();

			}

			transaction.close ();

			int numRetrieved = 0;

			for (ForwarderMessageInRec forwarderMessageIn
					: forwarderMessageIns) {

				numRetrieved ++;

				if (
					activeIds.contains (
						forwarderMessageIn.getId ())
				) {
					continue;
				}

				buffer.add (
					forwarderMessageIn.getId (),
					forwarderMessageIn.getId ());

			}

			return numRetrieved == buffer.getFullSize ();

		}

		@Override
		public
		void run () {

			while (true) {

				boolean moreMessages = doQuery();

				try {
					if (moreMessages)
						buffer.waitNotFull();
					else
						Thread.sleep(1000);
				} catch (InterruptedException e) {
					return;
				}

			}

		}

	}

	private static
	Pattern paramsPattern =
		Pattern.compile (
			"\\{(message|numfrom|numto|in_id)\\}");

	private static
	Pattern successPattern =
		Pattern.compile (
			"\\s*ok\\b",
			Pattern.CASE_INSENSITIVE);

	private
	class WorkerThread
		implements Runnable {

		private
		String getParams (
				ForwarderMessageInRec forwarderMessageIn) {

			try {

				StringBuilder stringBuilder =
					new StringBuilder ();

				Matcher matcher =
					paramsPattern.matcher (
						forwarderMessageIn
							.getForwarder ()
							.getUrlParams ());

				int i = 0;

				while (matcher.find ()) {

					stringBuilder.append (
						forwarderMessageIn
							.getForwarder ()
							.getUrlParams ()
							.substring (
								i,
								matcher.start ()));

					String paramName =
						matcher.group (1);

					if (paramName.equals("message")) {

						stringBuilder.append (
							URLEncoder.encode (
								forwarderMessageIn
									.getMessage ()
									.getText ()
									.getText (),
								"utf-8"));

					} else if (paramName.equals("num_from")
							|| paramName.equals("numfrom")) {

						stringBuilder.append(URLEncoder.encode(forwarderMessageIn.getMessage()
								.getNumFrom(), "utf-8"));

					} else if (paramName.equals("num_to")
							|| paramName.equals("numto")) {

						stringBuilder.append (
							URLEncoder.encode (
								forwarderMessageIn
									.getMessage ()
									.getNumTo (),
								"utf-8"));

					} else if (paramName.equals("in_id")) {

						stringBuilder.append (
							URLEncoder.encode (
								Integer.toString (
									forwarderMessageIn.getId ()),
								"utf-8"));

					}

					i = matcher.end ();

				}

				stringBuilder.append (
					forwarderMessageIn
						.getForwarder ()
						.getUrlParams ()
						.substring (i));

				return stringBuilder.toString();

			} catch (UnsupportedEncodingException exception) {

				throw new RuntimeException (
					"Error encoding request params",
					exception);

			}

		}

		private
		HttpURLConnection openPost (
				ForwarderMessageInRec forwarderMessageIn)
			throws IOException {

			String params =
				getParams (forwarderMessageIn);

			// create and open URL connection

			URL urlObj =
				new URL (
					forwarderMessageIn
						.getForwarder ()
						.getUrl ());

			HttpURLConnection urlConn =
				(HttpURLConnection)
				urlObj.openConnection ();

			urlConn.setDoInput (
				true);

			urlConn.setDoOutput (
				true);

			urlConn.setAllowUserInteraction (
				false);

			urlConn.setRequestMethod (
				"POST");

			urlConn.setRequestProperty (
				"User-Agent",
				wbsConfig.httpUserAgent ());

			urlConn.setRequestProperty (
				"Content-Type",
				"application/x-www-form-urlencoded");

			urlConn.setRequestProperty (
				"Content-Length",
				Integer.toString (
					params.length ()));

			// send request

			Writer out =
				new OutputStreamWriter (
					urlConn.getOutputStream (),
					"iso-8859-1");

			out.write (params);

			out.flush ();

			return urlConn;

		}

		private
		HttpURLConnection openGet (
				ForwarderMessageInRec forwarderMessageIn)
			throws IOException {

			URL urlObj =
				new URL (
					stringFormat (
						"%s",
						forwarderMessageIn.getForwarder ().getUrl (),
						"?%s",
						getParams (forwarderMessageIn)));

			HttpURLConnection urlConnection =
				(HttpURLConnection)
				urlObj.openConnection ();

			urlConnection.setDoInput (
				true);

			urlConnection.setAllowUserInteraction (
				false);

			urlConnection.setRequestMethod (
				"GET");

			urlConnection.setRequestProperty (
				"User-Agent",
				wbsConfig.httpUserAgent ());

			return urlConnection;

		}

		private
		boolean doSend (
				@NonNull ForwarderMessageInRec forwarderMessageIn) {

			if (
				forwarderMessageIn.getForwarder ().getUrl ().length () == 0
				|| forwarderMessageIn.getForwarder ().getUrlParams ().length () == 0
			) {

				return false;

			}

			try {

				HttpURLConnection urlConnection =
					forwarderMessageIn.getForwarder ().getUrlPost ()
						? openPost (forwarderMessageIn)
						: openGet (forwarderMessageIn);

				Reader in =
					new InputStreamReader (
						urlConnection.getInputStream (),
						"utf-8");

				StringBuffer responseBuffer =
					new StringBuffer ();

				int numread;
				char[] buffer = new char[1024];
				while ((numread = in.read(buffer, 0, 1024)) > 0)
					responseBuffer.append(buffer, 0, numread);
				String response = responseBuffer.toString();
				return successPattern.matcher(response).matches();

			} catch (IOException exception) {

				log.warn("IO exception forwarding message: " + exception);

				return false;

			}

		}

		private
		void doMessage (
				@NonNull Integer forwarderMessageInId) {

			@Cleanup
			Transaction checkTransaction =
				database.beginReadWrite (
					"ForwarderDaemon.WorkerThread.doMessage (forwarderMessageInId)",
					this);

			ForwarderMessageInRec forwarderMessageIn =
				forwarderMessageInHelper.findRequired (
					forwarderMessageInId);

			// check if we should cancel it

			long timeout =
				forwarderMessageIn
					.getForwarder ()
					.getInboundTimeoutSecs ();

			if (

				timeout > 0

				&& earlierThan (
					forwarderMessageIn.getCreatedTime ().plus (
						Duration.standardSeconds (
							timeout)),
					checkTransaction.now ())
			) {

				forwarderMessageIn =
					forwarderMessageInHelper.findRequired (
						forwarderMessageIn.getId ());

				forwarderMessageIn

					.setCancelledTime (
						checkTransaction.now ());

				checkTransaction.commit ();

				return;

			} else {

				checkTransaction.commit ();

			}

			// send it

			boolean success =
				doSend (
					forwarderMessageIn);

			@Cleanup
			Transaction resultTransaction =
				database.beginReadWrite (
					"ForwarderDaemon.WorkerThread.doMessage",
					this);

			doResult (
				forwarderMessageIn.getId (),
				success);

			resultTransaction.commit ();

		}

		private
		void doResult (
				int forwarderMessageInId,
				boolean success) {

			Transaction transaction =
				database.currentTransaction ();

			ForwarderMessageInRec forwarderMessageIn =
				forwarderMessageInHelper.findRequired (
					forwarderMessageInId);

			if (! forwarderMessageIn.getPending ())
				return;

			if (success) {

				forwarderMessageIn

					.setPending (
						false)

					.setSendQueue (
						false)

					.setProcessedTime (
						transaction.now ())

					.setRetryTime (
						null);

			} else {

				Calendar calendar =
					Calendar.getInstance ();

				calendar.add (Calendar.MINUTE, 1);

				forwarderMessageIn

					.setRetryTime (
						transaction.now ().plus (
							Duration.standardMinutes (1)));

			}

		}

		@Override
		public
		void run () {

			while (true) {

				Integer forwarderMessageInId;

				try {

					forwarderMessageInId =
						buffer.next ();

				} catch (InterruptedException exception) {

					return;

				}

				doMessage (
					forwarderMessageInId);

				buffer.remove (
					forwarderMessageInId);

			}

		}

	}

	@Override
	protected
	String getThreadName () {
		throw new UnsupportedOperationException ();
	}

	@Override
	protected
	void createThreads () {

		// main thread

		Thread mainThread;

		mainThread =
			threadManager.makeThread (
				new MainThread (),
				"Forwarder-Main");

		mainThread.setName (
			"ForwarderDaemon");

		mainThread.start ();

		registerThread (
			mainThread);

		// worker threads

		for (int i = 0; i < 1; i++) {

			Thread workerThread =
				threadManager.makeThread (
					new WorkerThread (),
					"Forwarder-" + i);

			workerThread.setName (
				"ForwarderDaemon worker " + i);

			workerThread.start ();

			registerThread (
				workerThread);

		}

	}

}
