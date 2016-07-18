package wbs.applications.imchat.hibernate;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import wbs.applications.imchat.model.ImChatConversationDao;
import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.framework.hibernate.HibernateDao;

public
class ImChatConversationDaoHibernate
	extends HibernateDao
	implements ImChatConversationDao {

	@Override
	public
	List<ImChatConversationRec> findPendingEmailLimit (
			int maxResults) {

		return findMany (
			"findPendingEmailLimit (maxResults)",
			ImChatConversationRec.class,

			createCriteria (
				ImChatConversationRec.class,
				"_imChatConversation")

			.add (
				Restrictions.isNotNull (
					"_imChatConversation.endTime"))

			.add (
				Restrictions.isNull (
					"_imChatConversation.emailTime"))

			.setMaxResults (
				maxResults)

		);

	}

}
