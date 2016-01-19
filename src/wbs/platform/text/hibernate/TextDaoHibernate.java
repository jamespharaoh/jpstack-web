package wbs.platform.text.hibernate;

import org.hibernate.FlushMode;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.platform.text.model.TextDao;
import wbs.platform.text.model.TextRec;

@SingletonComponent ("textDao")
public
class TextDaoHibernate
	extends HibernateDao
	implements TextDao {

	@Override
	public
	TextRec findByText (
			String textValue) {

		return findOne (
			TextRec.class,

			createQuery (
				"FROM TextRec t " +
				"WHERE t.text = :text")

			.setString (
				"text",
				textValue)

			//.setCacheable (
			//	true)

			.setFlushMode (
				FlushMode.MANUAL)

			.list ());

	}

}
