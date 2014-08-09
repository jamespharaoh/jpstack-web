package wbs.apn.chat.tv.core.daemon;

import wbs.platform.daemon.AbstractDaemonService;

public
class ChatTvCarouselDaemon
	extends AbstractDaemonService {

	/*
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
	Random random;

	@Inject
	ThreadManager threadManager;

	Map<Integer,Long> schedule =
		new TreeMap<Integer,Long> ();

	long time;

	@Override
	protected
	String getThreadName () {
		return "ChatTvCarousel";
	}

	@Override
	public
	void runService () {

		log.debug ("Starting");

		time = System.currentTimeMillis ();
		time -= time % 1000L;

		for (;;) {

			time += 1000;

			long now = System.currentTimeMillis ();
			long wait = time - now;
			if (wait > 0) {
				try {
					Thread.sleep (wait);
				} catch (InterruptedException e) {
					break;
				}
			}

			mainLoop ();
		}

		log.debug ("Stopping");
	}

	public
	void mainLoop () {

		if (time % 60000L == 0L)
			reload ();

		for (Map.Entry<Integer,Long> entry
				: schedule.entrySet ()) {

			int chatId =
				entry.getKey ();

			long carouselDelay =
				entry.getValue ();

			if (time % carouselDelay != 0L)
				continue;

			log.debug ("Doing chat " + chatId);

			try {

				doChat (chatId);

			} catch (Exception exception) {

				exceptionLogic.logException (
					"daemon",
					"Chat TV carousel daemon",
					exception,
					null,
					false);
			}
		}
	}

	public
	void reload () {

		log.debug ("Reload");

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		List<ChatTvRec> chatTvs =
			chatTvHelper.findAll ();

		schedule.clear ();

		for (ChatTvRec chatTv : chatTvs) {

			schedule.put (
				chatTv.getId (),
				chatTv.getCarouselDelay () * 1000L);

		}

	}

	public
	void doChat (
			int chatId) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		ChatTvRec chatTv =
			chatTvHelper.find (
				chatId);

		ChatTvMessageRec message =
			selectMessage (chatTv);

		MediaRec media =
			message.getMedia ();

		ChatTvCarouselRec carousel =
			new ChatTvCarouselRec ();

		carousel.setChatTv (chatTv);
		carousel.setMessage (message);
		carousel.setTimestamp (transaction.timestamp ());

		chatTvDao.insertChatTvCarousel (carousel);

		message.setCarouselTime (transaction.timestamp ());
		message.setCarouselCount (message.getCarouselCount () + 1);

		ChatTvPicUploadedRec picUploaded =
			chatTvDao.findChatTvPicUploadedById (media.getId ());

		transaction.commit ();

		if (picUploaded == null) {
			chatTvClient.uploadPicture (chatTv, media);
			chatTvLogic.markPicUploadedTx (media.getId ());
		}

		chatTvClient.sendMessages (
			chatTv,
			Collections.singletonList (message),
			ChatTvClient.Mode.carousel);

	}

	public
	ChatTvMessageRec selectMessage (
			ChatTvRec chatTv) {

		List<ChatTvMessageRec> messages =
			selectMessages (chatTv);

		// first priority is oldest not yet sent

		ChatTvMessageRec firstNotYetSent =
			findFirstNotYetSent (messages);

		if (firstNotYetSent != null)
			return firstNotYetSent;

		// second priority is random choice

		return findRandom (chatTv, messages);
	}

	public
	ChatTvMessageRec findFirstNotYetSent (
			List<ChatTvMessageRec> messages) {

		ChatTvMessageRec ret = null;

		for (ChatTvMessageRec message : messages) {

			if (message.getCarouselTime () != null)
				continue;

			if (ret == null
					|| message.getModeratedTime ().getTime ()
						< ret.getModeratedTime ().getTime ())
				ret = message;
		}

		return ret;
	}

	public
	ChatTvMessageRec findRandom (
			ChatTvRec chatTv,
			List<ChatTvMessageRec> messages) {

		// sort, least recent to carousel first

		Collections.sort (
			messages,
			messageComparator);

		// work out the range to select from

		int max = (100 - chatTv.getCarouselPercent ())
			* messages.size () / 100;

		// pick a random message

		int index =
			random.nextInt (max);

		return messages.get (index);

	}

	Comparator<ChatTvMessageRec> messageComparator =
		new Comparator<ChatTvMessageRec> () {

		@Override
		public
		int compare (
				ChatTvMessageRec left,
				ChatTvMessageRec right) {

			return left.getCarouselTime ().compareTo (
				right.getCarouselTime ());

		}

	};

	public
	List<ChatTvMessageRec> selectMessages (
			ChatTvRec chatTv) {

		Calendar calendar =
			new GregorianCalendar ();

		calendar.add (
			Calendar.SECOND,
			- chatTv.getCarouselOldest ());

		List<ChatTvMessageRec> messages =
			chatTvDao.findChatMessagesForCarousel (
				chatTv.getId (),
				calendar.getTime (),
				chatTv.getCarouselMaximum ());

		if (messages.size () < chatTv.getCarouselMinimum ())
			messages =
				chatTvDao.findChatMessagesForCarousel (
					chatTv.getId (),
					new Date (0),
					chatTv.getCarouselMaximum ());

		return messages;

	}
	*/

}
