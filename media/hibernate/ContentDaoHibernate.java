package wbs.platform.media.hibernate;

import java.util.List;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.media.model.ContentDao;
import wbs.platform.media.model.ContentRec;

public
class ContentDaoHibernate
	extends HibernateDao
	implements ContentDao {

	@Override
	public
	List<ContentRec> findByHash (
			int hash) {

		return findMany (
			ContentRec.class,

			createQuery (
				"FROM ContentRec content " +
				"WHERE content.hash = :hash")

			.setInteger (
				"hash",
				hash)

			.list ());

	}

}
