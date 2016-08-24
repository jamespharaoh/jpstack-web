package wbs.platform.object.core.hibernate;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import lombok.NonNull;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.entity.record.Record;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.object.ObjectTypeRegistry;
import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.scaffold.model.RootRec;

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
			@NonNull Long id) {

		return get (
			ObjectTypeRec.class,
			id);

	}

	@Override
	public
	ObjectTypeRec findByCode (
			@NonNull String code) {

		return findOne (
			"findByCode (code)",
			ObjectTypeRec.class,

			createCriteria (
				ObjectTypeRec.class,
				"_objectType")

			.add (
				Restrictions.eq (
					"_objectType.code",
					code))

			.setCacheable (
				true)

		);

	}

	@Override
	public
	List<ObjectTypeRec> findAll () {

		return findMany (
			"findAll ()",
			ObjectTypeRec.class,

			createCriteria (
				ObjectTypeRec.class)

		);

	}

	@Override
	public
	Class<? extends Record<?>> objectTypeRecordClass () {

		return ObjectTypeRec.class;

	}

	@Override
	public
	Class<? extends Record<?>> rootRecordClass () {

		return RootRec.class;

	}

}
