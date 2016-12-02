package wbs.platform.user.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

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
			@NonNull UserRec user,
			@NonNull PrivRec priv) {

		return findOneOrNull (
			"find (user, priv)",
			UserPrivRec.class,

			createCriteria (
				UserPrivRec.class,
				"_userPriv")

			.add (
				Restrictions.eq (
					"_userPriv.user",
					user))

			.add (
				Restrictions.eq (
					"_userPriv.priv",
					priv))

		);

	}

}
