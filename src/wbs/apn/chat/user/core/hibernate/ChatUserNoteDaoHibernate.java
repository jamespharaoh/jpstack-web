package wbs.apn.chat.user.core.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import wbs.apn.chat.user.core.model.ChatUserNoteDao;
import wbs.apn.chat.user.core.model.ChatUserNoteRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatUserNoteDaoHibernate
	extends HibernateDao
	implements ChatUserNoteDao {

	@Override
	public
	List<ChatUserNoteRec> find (
			@NonNull ChatUserRec chatUser) {

		return findMany (
			"find (chatUser)",
			ChatUserNoteRec.class,

			createCriteria (
				ChatUserNoteRec.class,
				"_chatUserNote")

			.add (
				Restrictions.eq (
					"_chatUserNote.chatUser",
					chatUser))

			.addOrder (
				Order.desc (
					"_chatUserNote.timestamp"))

		);

	}

}
