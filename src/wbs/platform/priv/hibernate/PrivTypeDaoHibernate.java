package wbs.platform.priv.hibernate;

import java.util.List;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.priv.model.PrivTypeDao;
import wbs.platform.priv.model.PrivTypeRec;

public
class PrivTypeDaoHibernate
	extends HibernateDao
	implements PrivTypeDao {

	@Override
	public
	List<PrivTypeRec> findByParentObjectType (
			ObjectTypeRec parentObjectType) {

		return findMany (
			PrivTypeRec.class,

			createQuery (
				"FROM PrivTypeRec privType " +
				"WHERE privType.parentObjectType = :parentObjectType")

			.setEntity (
				"parentObjectType",
				parentObjectType)

			.list ());

	}

}
