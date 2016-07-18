package wbs.platform.updatelog.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.application.annotations.SingletonComponent;
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
			long ref) {

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
					(int) ref))

		);

	}

}
