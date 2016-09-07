package wbs.platform.updatelog.hibernate;

import org.hibernate.criterion.Restrictions;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.platform.updatelog.model.UpdateLogDao;
import wbs.platform.updatelog.model.UpdateLogRec;

@SingletonComponent ("updateLogDao")
public
class UpdateLogDaoHibernate
	extends HibernateDao
	implements UpdateLogDao {

	@Override
	public
	UpdateLogRec findByTableAndRef (
			@NonNull String table,
			@NonNull Long ref) {

		return findOne (
			"findByTableAndRef (table, ref)",
			UpdateLogRec.class,

			createCriteria (
				UpdateLogRec.class,
				"_updateLog")

			.add (
				Restrictions.eq (
					"_updateLog.code",
					table))

			.add (
				Restrictions.eq (
					"_updateLog.ref",
					ref))

		);

	}

}
