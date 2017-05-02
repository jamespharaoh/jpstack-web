package wbs.imchat.hibernate;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.imchat.model.ImChatConversationDao;
import wbs.imchat.model.ImChatConversationRec;

public
class ImChatConversationDaoHibernate
	extends HibernateDao
	implements ImChatConversationDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <ImChatConversationRec> findPendingEmailLimit (
			@NonNull Transaction parentTransaction,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findPendingEmailLimit");

		) {

			return findMany (
				transaction,
				ImChatConversationRec.class,

				createCriteria (
					transaction,
					ImChatConversationRec.class,
					"_imChatConversation")

				.add (
					Restrictions.isNotNull (
						"_imChatConversation.endTime"))

				.add (
					Restrictions.isNull (
						"_imChatConversation.emailTime"))

				.setMaxResults (
					toJavaIntegerRequired (
						maxResults))

			);

		}

	}

}
