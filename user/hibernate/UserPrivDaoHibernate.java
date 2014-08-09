package wbs.platform.user.hibernate;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.priv.model.PrivRec;
import wbs.platform.user.model.UserPrivDao;
import wbs.platform.user.model.UserPrivRec;
import wbs.platform.user.model.UserRec;

public
class UserPrivDaoHibernate
	extends HibernateDao
	implements UserPrivDao {

	@Override
	public
	UserPrivRec find (
			UserRec user,
			PrivRec priv) {

		return findOne (
			UserPrivRec.class,

			createQuery (
				"FROM UserPrivRec userPriv " +
				"WHERE userPriv.user = :user " +
					"AND userPriv.priv = :priv")

			.setEntity (
				"user",
				user)

			.setEntity (
				"priv",
				priv)

			.list ());

	}

}
