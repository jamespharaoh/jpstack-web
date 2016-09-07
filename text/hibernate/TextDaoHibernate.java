package wbs.platform.text.hibernate;

import lombok.NonNull;

import org.hibernate.FlushMode;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.SingletonComponent;
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
	TextRec findByTextNoFlush (
			@NonNull String textValue) {

		return findOne (
			"findByText (textValue)",
			TextRec.class,

			createCriteria (
				TextRec.class,
				"_text")

			.add (
				Restrictions.eq (
					"_text.text",
					textValue))

			.setFlushMode (
				FlushMode.MANUAL)

		);

	}

}
