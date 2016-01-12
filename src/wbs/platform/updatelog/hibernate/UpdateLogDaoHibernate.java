package wbs.platform.updatelog.hibernate;

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
			String table,
			long ref) {

		return findOne (
			UpdateLogRec.class,

			createQuery (
				"FROM UpdateLogRec updateLog " +
				"WHERE updateLog.code = :code " +
					"AND ref = :ref")

			.setString (
				"code",
				table)

			.setInteger (
				"ref",
				(int) ref)

			.list ());

	}

}
