package wbs.apn.chat.tv.core.hibernate;

import wbs.apn.chat.tv.core.model.ChatTvDao;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;

@SingletonComponent ("chatTvDao")
public
class ChatTvDaoHibernate
	extends HibernateDao
	implements ChatTvDao {

	/*
	@Override
	public List<ChatTvOutboxRec> findMessageOutboxesToSend (
			int chatId,
			int limit) {

		return findMany (
			ChatTvOutboxRec.class,

			createQuery (
				"FROM ChatTvOutboxRec outbox " +
					"LEFT JOIN FETCH outbox.message message " +
					"LEFT JOIN FETCH message.editedText " +
					"LEFT JOIN FETCH message.chatUser chatUser " +
					"LEFT JOIN FETCH chatUser.chat " +
				"WHERE outbox.message.chatUser.chat.id = :chatId " +
					"AND outbox.sendingToken IS NULL " +
				"ORDER BY outbox.id")

			.setInteger ("chatId", chatId)

			.setMaxResults (limit)

			.list ());

	}

	@Override
	public ChatTvOutboxRec findMessageOutboxToSend () {

		return findOne (
			ChatTvOutboxRec.class,

			createQuery (
				"FROM ChatTvOutboxRec outbox " +
					"LEFT JOIN FETCH outbox.message message " +
					"LEFT JOIN FETCH message.editedText " +
					"LEFT JOIN FETCH message.chatUser chatUser " +
					"LEFT JOIN FETCH chatUser.chat " +
				"WHERE outbox.sendingToken IS NULL " +
				"ORDER BY outbox.id")

			.setMaxResults (1)

			.list ());

	}

	@Override
	public List<ChatTvMessageRec> findMessagesRecent (
			int chatId,
			int limit) {

		return findMany (
			ChatTvMessageRec.class,

			createQuery (
				"FROM ChatTvMessageRec message " +
					"LEFT JOIN FETCH message.editedText " +
					"LEFT JOIN FETCH message.chatUser chatUser " +
					"LEFT JOIN FETCH chatUser.chat " +
				"WHERE message.status IN (:messageStatuses) " +
				"ORDER BY message.moderatedTime DESC")

			.setParameterList (
				"messageStatuses",
				Arrays.asList (
					ChatTvMessageStatus.holding,
					ChatTvMessageStatus.sending,
					ChatTvMessageStatus.sent),
				ChatTvMessageStatusType.INSTANCE)

			.setMaxResults (limit)

			.list ());

	}

	@Override
	public ChatTvMessageRec findMessageForModeration (
			int chatUserId) {

		return findOne (
			ChatTvMessageRec.class,

			createQuery (
				"FROM ChatTvMessageRec message " +
				"WHERE message.status = :messageStatus " +
					"AND message.chatUser.id = :chatUserId " +
				"ORDER BY message.id")

			.setParameter (
				"messageStatus",
				ChatTvMessageStatus.moderating,
				ChatTvMessageStatusType.INSTANCE)

			.setInteger ("chatUserId", chatUserId)

			.setMaxResults (1)

			.list ());

	}

	@Override
	public List<ChatTvMessageRec> findMessagesRecentByUser (
			int chatId,
			int chatUserId,
			int limit) {

		return findMany (
			ChatTvMessageRec.class,

			createQuery (
				"FROM ChatTvMessageRec message " +
					"LEFT JOIN FETCH message.editedText " +
					"LEFT JOIN FETCH message.chatUser chatUser " +
					"LEFT JOIN FETCH chatUser.chat " +
				"WHERE message.chatUser.id = :chatUserId " +
				"ORDER BY message.createdTime DESC")

			.setInteger ("chatUserId", chatUserId)
			.setMaxResults (limit)

			.list ());

	}

	@Override
	public List<ChatTvMessageRec> findMessagesForTimeout (
			int chatId,
			Date createdTime) {

		return findMany (
			ChatTvMessageRec.class,

			createQuery (
				"FROM ChatTvMessageRec message " +
				"WHERE message.chatUser.chat.id = :chatId " +
					"AND message.status = :messageStatus " +
					"AND message.createdTime < :createdTime")

			.setInteger ("chatId", chatId)

			.setParameter (
				"messageStatus",
				ChatTvMessageStatus.signup,
				ChatTvMessageStatusType.INSTANCE)

			.setTimestamp ("createdTime", createdTime)

			.list ());

	}

	@Override
	public ChatTvMessageRec findMessageForSignup (
			int chatUserId) {

		return findOne (
			ChatTvMessageRec.class,

			createQuery (
				"FROM ChatTvMessageRec message " +
				"WHERE message.status = :messageStatus " +
					"AND message.chatUser.id = :chatUserId")

			.setParameter (
				"messageStatus",
				ChatTvMessageStatus.signup,
				ChatTvMessageStatusType.INSTANCE)

			.setInteger ("chatUserId", chatUserId)

			.list ());

	}

	@Override
	public List<ChatTvMessageRec> findChatMessagesForCarousel (
			int chatId,
			Date timestamp,
			int maximum) {

		return findMany (
			ChatTvMessageRec.class,

			createQuery (
				"FROM ChatTvMessageRec message " +
				"WHERE message.status IN (:messageStatuses) " +
					"AND message.media IS NOT NULL " +
				"ORDER BY message.carouselTime DESC")

			.setParameterList (
				"messageStatuses",
				Arrays.asList (
					ChatTvMessageStatus.approved,
					ChatTvMessageStatus.sending,
					ChatTvMessageStatus.sent),
				ChatTvMessageStatusType.INSTANCE)

			.setMaxResults (maximum)

			.list ());

	}

	@Override
	public ChatTvUserSpendRec findChatTvUserSpend (
			int chatUserId,
			LocalDate date) {

		return findOne (
			ChatTvUserSpendRec.class,

			createQuery (
				"FROM ChatTvUserSpendRec spend " +
				"WHERE spend.chatTvUser.id = :chatUserId " +
					"AND date = :date")

			.setInteger ("chatUserId", chatUserId)

			.setParameter (
				"date",
				date,
				new CustomType (new PersistentLocalDate ())) // TODO ??

			.list ());

	}
	*/

}
