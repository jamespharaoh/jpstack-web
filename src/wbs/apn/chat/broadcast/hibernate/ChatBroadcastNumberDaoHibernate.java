package wbs.apn.chat.broadcast.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.apn.chat.broadcast.model.ChatBroadcastNumberState;
import wbs.apn.chat.broadcast.model.ChatBroadcastNumberDao;
import wbs.apn.chat.broadcast.model.ChatBroadcastNumberRec;
import wbs.apn.chat.broadcast.model.ChatBroadcastRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatBroadcastNumberDaoHibernate
	extends HibernateDao
	implements ChatBroadcastNumberDao {

	@Override
	public
	List<ChatBroadcastNumberRec> findAcceptedLimit (
			@NonNull ChatBroadcastRec chatBroadcast,
			int maxResults) {

		return findMany (
			"findAcceptedLimit (chatBroadcast, maxResults)",
			ChatBroadcastNumberRec.class,

			createCriteria (
				ChatBroadcastNumberRec.class,
				"_chatBroadcastNumber")

			.add (
				Restrictions.eq (
					"_chatBroadcastNumber.chatBroadcast",
					chatBroadcast))

			.add (
				Restrictions.eq (
					"_chatBroadcastNumber.state",
					ChatBroadcastNumberState.accepted))

			.setMaxResults (
				maxResults)

		);

	}

}
