package wbs.apn.chat.contact.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Interval;

import wbs.apn.chat.contact.model.ChatContactNoteDao;
import wbs.apn.chat.contact.model.ChatContactNoteRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatContactNoteDaoHibernate
	extends HibernateDao
	implements ChatContactNoteDao {

	@Override
	public
	List<ChatContactNoteRec> findByTimestamp (
			@NonNull ChatRec chat,
			@NonNull Interval timestamp) {

		return findMany (
			"findByTimestamp (chat, timestamp)",
			ChatContactNoteRec.class,

			createCriteria (
				ChatContactNoteRec.class,
				"_chatContactNote")

			.add (
				Restrictions.eq (
					"_chatContactNote.chat",
					chat))

			.add (
				Restrictions.ge (
					"_chatContactNote.timestamp",
					timestamp.getStart ()))

			.add (
				Restrictions.lt (
					"_chatContactNote.timestamp",
					timestamp.getEnd ()))

			.addOrder (
				Order.asc (
					"_chatContactNote.timestamp"))

		);

	}

	@Override
	public
	List<ChatContactNoteRec> find (
			@NonNull ChatUserRec userChatUser,
			@NonNull ChatUserRec monitorChatUser) {

		return findMany (
			"find (userChatUser, monitorChatUser)",
			ChatContactNoteRec.class,

			createCriteria (
				ChatContactNoteRec.class,
				"_chatContactNote")

			.add (
				Restrictions.eq (
					"_chatContactNote.user",
					userChatUser))

			.add (
				Restrictions.eq (
					"_chatContactNote.monitor",
					monitorChatUser))

			.addOrder (
				Order.desc (
					"_chatContactNote.pegged"))

			.addOrder (
				Order.asc (
					"_chatContactNote.timestamp"))

		);

	}


}
