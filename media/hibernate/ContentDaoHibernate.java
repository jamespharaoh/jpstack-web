package wbs.platform.media.hibernate;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.media.model.ContentDao;
import wbs.platform.media.model.ContentRec;

public
class ContentDaoHibernate
	extends HibernateDao
	implements ContentDao {

	@Override
	public
	List <ContentRec> findByShortHash (
			Long shortHash) {

		return findMany (
			"findByHash (hash)",
			ContentRec.class,

			createCriteria (
				ContentRec.class,
				"_content")

			.add (
				Restrictions.eq (
					"_content.hash",
					shortHash))

		);

	}

}
