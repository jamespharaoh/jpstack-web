package wbs.platform.object.core.hibernate;

import java.util.List;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.object.ObjectTypeRegistry;
import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;

@SingletonComponent ("objectTypeDao")
public
class ObjectTypeDaoHibernate
	extends HibernateDao
	implements
		ObjectTypeDao,
		ObjectTypeRegistry {

	@Override
	public
	ObjectTypeRec findById (
			int id) {

		return get (
			ObjectTypeRec.class,
			id);

	}

	@Override
	public
	ObjectTypeRec findByCode (
			String code) {

		List<?> list =
			createQuery (
				"FROM ObjectTypeRec dot " +
				"WHERE dot.code = :code")
			.setString ("code", code)
			.setCacheable (true)
			.list ();

		if (list.isEmpty ())
			return null;

		return (ObjectTypeRec)
			list.get (0);

	}

	@Override
	public
	List<ObjectTypeRec> findAll () {

		return findMany (
			ObjectTypeRec.class,

			createQuery (
				"FROM ObjectTypeRec")

			.list ());

	}

}
