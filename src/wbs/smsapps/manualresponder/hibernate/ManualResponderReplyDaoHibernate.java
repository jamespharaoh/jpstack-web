package wbs.smsapps.manualresponder.hibernate;

import java.util.List;

import javax.annotation.Nonnull;

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
import wbs.smsapps.manualresponder.model.ManualResponderReplyStatsSearch;
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
			@Nonnull Transaction parentTransaction,
			@Nonnull ManualResponderReplyStatsSearch search) {

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

			// apply criteria

			criteria.add (
				Restrictions.in (
					"_manualResponder.id",
					search.manualResponderIds ()));

			criteria.add (
				Restrictions.in (
					"_user.id",
					search.userIds ()));

			criteria.add (
				Restrictions.ge (
					"_manualResponderReplyView.timestamp",
					search.timestamp ().start ()));

			criteria.add (
				Restrictions.lt (
					"_manualResponderReplyView.timestamp",
					search.timestamp ().end ()));

			// apply filter

			criteria.add (
				Restrictions.or (

				Restrictions.in (
					"_manualResponder.id",
					search.filterManualResponderIds ()),

				Restrictions.in (
					"_user.id",
					search.filterUserIds ())

			));

			// apply projection

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

			// return

			return findMany (
				transaction,
				ManualResponderReplyUserStats.class,
				criteria);

		}

	}

}
