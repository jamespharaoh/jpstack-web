package wbs.apn.chat.namednote.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.namednote.model.ChatNoteNameDao;
import wbs.apn.chat.namednote.model.ChatNoteNameRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatNoteNameDaoHibernate
	extends HibernateDao
	implements ChatNoteNameDao {

	@Override
	public
	List<ChatNoteNameRec> findNotDeleted (
			@NonNull ChatRec chat) {

		return findMany (
			"findNotDeleted (chat)",
			ChatNoteNameRec.class,

			createCriteria (
				ChatNoteNameRec.class,
				"_chatNoteName")

			.add (
				Restrictions.eq (
					"_chatNoteName.chat",
					chat))

			.add (
				Restrictions.eq (
					"_chatNoteName.deleted",
					false))

			.addOrder (
				Order.asc (
					"_chatNoteName.index"))

		);

	}

}
