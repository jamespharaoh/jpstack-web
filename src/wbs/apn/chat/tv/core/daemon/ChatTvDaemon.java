package wbs.apn.chat.tv.core.daemon;

import wbs.platform.daemon.AbstractDaemonService;

public
class ChatTvDaemon
	extends AbstractDaemonService {

	/*

	public final static
	int ONE_MILLISECOND = 1;

	public final static
	int TEN_MILLISECONDS = 10 * ONE_MILLISECOND;

	public final static
	int ONE_SECOND = 1000;

	public final static
	int ZERO = 0;

	public final static
	int NORMAL_SLEEP_TIME = ONE_SECOND;

	public final static
	int ERROR_SLEEP_TIME = 5 * ONE_SECOND;

	public final static
	int BATCH_SIZE = 20;

	public final static
	int DB_TRIES_COUNT = 3;

	public final static
	int DB_TRIES_DELAY = TEN_MILLISECONDS;

	@Inject
	ChatTvClient chatTvClient;

	@Inject
	ChatTvDao chatTvDao;

	@Inject
	ChatTvObjectHelper chatTvHelper;

	@Inject
	ChatTvLogic chatTvLogic;

	@Inject
	Database database;

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	MediaDao mediaDao;

	int pid;
	String hostname;

	@Override
	protected
	String getThreadName () {
		return "ChatTvDaemon";
	}

	@Override
	protected
	void init () {

		// get hostname
		try {
			hostname = InetAddress.getLocalHost ().getHostName ();
		} catch (UnknownHostException e) {
			throw new RuntimeException (e);
		}

		// get pid
		try {
			String[] cmd = { "bash", "-c", "echo $PPID" };
			Process p = Runtime.getRuntime ().exec (cmd);
			String output = IOUtils.toString (p.getInputStream ());
			pid = Integer.parseInt (output.trim ());
		} catch (IOException e) {
			throw new RuntimeException (e);
		}

		log.debug (sf ("Got hostname \"%s\" and pid \"%d\"", hostname, pid));
	}

	@Override
	public
	void runService () {
		for (;;) {

			// perform a single round
			int timeToSleep;
			try {
				timeToSleep = mainLoop ();
			} catch (Exception e) {
				log.error (e);
				exceptionLogic.logException (
					"daemon",
					"Chat TV daemon",
					e,
					null,
					false);
				timeToSleep = ERROR_SLEEP_TIME;
			}

			// sleep a while
			try {
				Thread.sleep (timeToSleep);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	private
	long nextTimeout = 0;

	public
	int mainLoop ()
		throws IOException {

		String token =
			Misc.genToken ();

		// do timeouts every minute
		if (nextTimeout < System.currentTimeMillis ()) {
			doTimeouts ();
			nextTimeout = System.currentTimeMillis () + 60 * 1000;
		}

		// get messages from database

		log.debug ("Polling database");

		FindOutboxesResult findOutboxesResult =
			findOutboxes (token);

		if (findOutboxesResult == null)
			return NORMAL_SLEEP_TIME;

		List<ChatTvOutboxRec> outboxes =
			findOutboxesResult.outboxes;

		// upload pictures

		if (findOutboxesResult.uploads != null) {

			for (MediaRec media
					: findOutboxesResult.uploads) {

				chatTvClient.uploadPicture (
					findOutboxesResult.chatTv,
					media);

				chatTvLogic.markPicUploadedTx (
					media.getId ());

			}

			return ZERO;

		}

		boolean success = false;

		try {

			ChatTvRec chatTv =
				findOutboxesResult.chatTv;

			List<ChatTvMessageRec> messages =
				new ArrayList<ChatTvMessageRec> ();

			for (ChatTvOutboxRec outbox
					: findOutboxesResult.outboxes) {

				messages.add (
					outbox.getMessage ());

			}

			chatTvClient.sendMessages (
				chatTv,
				messages,
				ChatTvClient.Mode.feed);

			success = true;

		} finally {

			// process response

			Tries tries =
				new Tries (
					DB_TRIES_COUNT,
					DB_TRIES_DELAY);

			while (tries.next ()) {

				try {

					processResponse (
						token,
						outboxes,
						success);

					tries.done ();

				} catch (RuntimeException exception) {

					tries.error (exception);

				}

			}

		}

		// loop again immediately

		return ZERO;

	}

	public
	void doTimeouts () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		log.debug ("Checkout for timeouts");

		List<ChatTvRec> chatTvs =
			chatTvHelper.findAll ();

		for (ChatTvRec chatTv
				: chatTvs) {

			long createdTimeMillis =
				+ System.currentTimeMillis ()
				- chatTv.getToScreenTimeout () * 1000;

			Date createdTime =
				new Date (createdTimeMillis);

			log.debug (
				sf ("Chat %s (%d), created time %s",
					chatTv.getChat ().getCode (),
					chatTv.getId (),
					?.formatTimestamp (createdTime)));

			List<ChatTvMessageRec> messages =
				chatTvDao.findMessagesForTimeout (
					chatTv.getId (), createdTime);

			for (ChatTvMessageRec message : messages) {

				log.info ("Timing out message " + message.getId ());

				message.setStatus (ChatTvMessageStatus.timeout);

			}
		}

		transaction.commit ();

	}

	class FindOutboxesResult {
		List<MediaRec> uploads;
		List<ChatTvOutboxRec> outboxes;
		ChatTvRec chatTv;
	}

	public
	FindOutboxesResult findOutboxes (
			String token) {

		FindOutboxesResult ret =
			new FindOutboxesResult ();

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		// get one outbox from database

		ChatTvOutboxRec firstOutbox =
			chatTvDao.findMessageOutboxToSend ();

		if (firstOutbox == null)
			return null;

		// find the chat and chatTv

		ChatRec chat =
			firstOutbox.getMessage ().getChatUser ().getChat ();

		ret.chatTv =
			chatTvHelper.find (
				chat.getId ());

		// now get more with the same chatId

		log.debug ("Fetching messages with chat id " + chat.getId ());

		ret.outboxes =
			chatTvDao.findMessageOutboxesToSend (
				chat.getId (),
				BATCH_SIZE);

		// find referenced medias

		Set<MediaRec> medias =
			new HashSet<MediaRec> ();

		for (ChatTvOutboxRec outbox : ret.outboxes) {

			ChatTvMessageRec message =
				outbox.getMessage ();

			if (message.getMedia () != null)
				medias.add (message.getMedia ());

			ChatUserRec chatUser =
				message.getChatUser ();

			ChatUserImageRec chatUserImage =
				chatUser.getMainChatUserImage ();

			if (chatUserImage != null)
				medias.add (chatUserImage.getMedia ());
		}

		// find medias which have not been uploaded

		ret.uploads =
			new ArrayList<MediaRec> ();

		for (MediaRec media : medias) {

			ChatTvPicUploadedRec picUploaded =
				chatTvDao.findChatTvPicUploadedById (media.getId ());

			if (picUploaded != null) continue;

			ret.uploads.add (media);

			media.getContent ().getData ();
			media.getMediaType ().getMimeType ();

		}

		// return list of medias to upload if any

		if (ret.uploads.isEmpty ()) {
			ret.uploads = null;
		} else {
			return ret;
		}

		// mark outboxes as sending

		for (ChatTvOutboxRec outbox
				: ret.outboxes) {

			outbox.setSendingToken (token);
			outbox.setSendingHostname (hostname);
			outbox.setSendingPid (pid);
			outbox.setSendingTime (transaction.timestamp ());

		}

		transaction.commit ();

		return ret;

	}

	public
	void processResponse (
			String token,
			List<ChatTvOutboxRec> outboxes,
			boolean success) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		for (ChatTvOutboxRec outbox
				: outboxes) {

			outbox =
				chatTvDao.findMessageOutboxById (
					outbox.getId ());

			ChatTvMessageRec message =
				outbox.getMessage ();

			// sanity check status

			if (message.getStatus () != ChatTvMessageStatus.sending) {

				throw new RuntimeException (
					sf ("Message %d status is %s",
						message.getId (),
						message.getStatus ()));

			}

			// sanity check token

			if (! equal (
					outbox.getSendingToken (),
					token)) {

				throw new RuntimeException (sf (
					"Message %d token is %s (%d on %s at %s)",
					message.getId (),
					outbox.getSendingToken (),
					outbox.getSendingPid (),
					outbox.getSendingHostname (),
					Misc.timestampFormatSeconds.format (
						outbox.getSendingTime ())));

			}

			// update database

			if (success) {

				message
					.setSentTime (transaction.timestamp ())
					.setStatus (ChatTvMessageStatus.sent);

				chatTvDao.removeMessageOutbox (
					outbox);

			} else {

				outbox
					.setSendingToken (null)
					.setSendingHostname (null)
					.setSendingPid (null)
					.setSendingTime (null);

			}

		}

		// commit transaction

		transaction.commit ();

	}
	*/

}
