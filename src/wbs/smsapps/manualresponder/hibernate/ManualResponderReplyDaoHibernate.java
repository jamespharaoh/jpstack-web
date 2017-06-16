package wbs.smsapps.manualresponder.hibernate;

import static wbs.utils.etc.NullUtils.isNotNull;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.smsapps.manualresponder.model.ManualResponderReplyDaoMethods;
import wbs.smsapps.manualresponder.model.ManualResponderReplySearch;
import wbs.smsapps.manualresponder.model.ManualResponderReplyUserStats;
import wbs.smsapps.manualresponder.model.ManualResponderReplyViewRec;

public
class ManualResponderReplyDaoHibernate
	extends HibernateDao
	implements ManualResponderReplyDaoMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	List <ManualResponderReplyUserStats> searchUserStats (
			@NonNull Transaction parentTransaction,
			@NonNull ManualResponderReplySearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchUserStats");

		) {

			Criteria criteria =
				createCriteria (
					transaction,
					ManualResponderReplyViewRec.class,
					"_manualResponderReplyView")

				.createAlias (
					"_manualResponderReplyView.manualResponder",
					"_manualResponder")

				.createAlias (
					"_manualResponderReplyView.user",
					"_user")

			;

			if (
				isNotNull (
					search.manualResponderIds ())
			) {

				criteria.add (
					Restrictions.in (
						"_manualResponder.id",
						search.manualResponderIds ()));

			}

			if (
				isNotNull (
					search.userIds ())
			) {

				criteria.add (
					Restrictions.in (
						"_user.id",
						search.userIds ()));

			}

			if (
				isNotNull (
					search.timestamp ())
			) {

				criteria.add (
					Restrictions.ge (
						"_manualResponderReplyView.timestamp",
						search.timestamp ().start ()));

				criteria.add (
					Restrictions.lt (
						"_manualResponderReplyView.timestamp",
						search.timestamp ().end ()));

			}

			criteria.setProjection (
				Projections.projectionList ()

				.add (
					Projections.groupProperty (
						"_manualResponderReplyView.manualResponder"),
					"manualResponder")

				.add (
					Projections.groupProperty (
						"_manualResponderReplyView.user"),
					"user")

				.add (
					Projections.rowCount (),
					"numReplies")

				.add (
					Projections.sum (
						"_manualResponderReplyView.numCharacters"),
					"numCharacters")

			);

			criteria.setResultTransformer (
				Transformers.aliasToBean (
					ManualResponderReplyUserStats.class));

			return findMany (
				transaction,
				ManualResponderReplyUserStats.class,
				criteria);

		}

	}

}
