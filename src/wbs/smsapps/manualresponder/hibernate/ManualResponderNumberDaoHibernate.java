package wbs.smsapps.manualresponder.hibernate;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import wbs.framework.hibernate.HibernateDao;

import wbs.sms.number.core.model.NumberRec;

import wbs.smsapps.manualresponder.model.ManualResponderNumberDao;
import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderNumberSearch;
import wbs.smsapps.manualresponder.model.ManualResponderRec;

public
class ManualResponderNumberDaoHibernate
	extends HibernateDao
	implements ManualResponderNumberDao {

	@Override
	public
	ManualResponderNumberRec find (
			@NonNull ManualResponderRec manualResponder,
			@NonNull NumberRec number) {

		return findOneOrNull (
			"findOne (manualResponder, number)",
			ManualResponderNumberRec.class,

			createCriteria (
				ManualResponderNumberRec.class,
				"_manualResponderNumber")

			.add (
				Restrictions.eq (
					"_manualResponderNumber.manualResponder",
					manualResponder))

			.add (
				Restrictions.eq (
					"_manualResponderNumber.number",
					number))

		);

	}

	@Override
	public
	List <Long> searchIds (
			@NonNull ManualResponderNumberSearch search) {

		Criteria criteria =
			createCriteria (
				ManualResponderNumberRec.class,
				"_manualResponderNumber")

			.createAlias (
				"_manualResponderNumber.number",
				"_number")

			.createAlias (
				"_manualResponderNumber.notesText",
				"_notesText",
				JoinType.LEFT_OUTER_JOIN);

		if (
			isNotNull (
				search.manualResponderId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_manualResponderNumber.manualResponder.id",
					search.manualResponderId ()));

		}

		if (
			isNotNull (
				search.number ())
		) {

			criteria.add (
				Restrictions.eq (
					"_number.number",
					search.number ()));

		}

		if (
			isNotNull (
				search.firstRequest ())
		) {

			criteria.add (
				Restrictions.ge (
					"_manualResponderNumber.firstRequest",
					search.firstRequest ().start ()));

			criteria.add (
				Restrictions.lt (
					"_manualResponderNumber.firstRequest",
					search.firstRequest ().end ()));

		}

		if (
			isNotNull (
				search.lastRequest ())
		) {

			criteria.add (
				Restrictions.ge (
					"_manualResponderNumber.lastRequest",
					search.lastRequest ().start ()));

			criteria.add (
				Restrictions.lt (
					"_manualResponderNumber.lastRequest",
					search.lastRequest ().end ()));

		}

		if (
			isNotNull (
				search.notes ())
		) {

			criteria.add (
				Restrictions.ilike (
					"_notesText.text",
					stringFormat (
						"%%%s%%",
						search.notes ())));

		}

		criteria.addOrder (
			Order.asc (
				"_number.number"));

		criteria.setProjection (
			Projections.id ());

		return findMany (
			"searchIds (search)",
			Long.class,
			criteria);

	}

}
