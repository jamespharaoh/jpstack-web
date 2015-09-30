package wbs.smsapps.manualresponder.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestDao;
import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestSearch;

public
class ManualResponderRequestDaoHibernate
	extends HibernateDao
	implements ManualResponderRequestDao {

	@Override
	public
	List<ManualResponderRequestRec> findRecentLimit (
			@NonNull ManualResponderRec manualResponder,
			@NonNull NumberRec number,
			@NonNull Integer maxResults) {

		return findMany (
			ManualResponderRequestRec.class,

			createCriteria (
				ManualResponderRequestRec.class,
				"_manualResponderRequest")

			.add (
				Restrictions.eq (
					"_manualResponderRequest.manualResponder",
					manualResponder))

			.add (
				Restrictions.eq (
					"_manualResponderRequest.number",
					number))

			.addOrder (
				Order.desc (
					"_manualResponderRequest.timestamp"))

			.addOrder (
				Order.desc (
					"_manualResponderRequest.id"))

			.setMaxResults (
				maxResults)

			.list ());

	}

	@Override
	public
	List<Integer> searchIds (
			ManualResponderRequestSearch search) {

		Criteria criteria =

			createCriteria (
				ManualResponderRequestRec.class,
				"_manualResponderRequest")

			.createAlias (
				"_manualResponderRequest.manualResponder",
				"_manualResponder")

			.createAlias (
				"_manualResponderRequest.number",
				"_number");

		if (search.manualResponderId () != null) {

			criteria.add (
				Restrictions.eq (
					"_manualResponder.id",
					search.manualResponderId ()));

		}

		if (search.numberLike () != null) {

			criteria.add (
				Restrictions.like (
					"_number.number",
					search.numberLike ()));

		}

		if (search.timestampAfter () != null) {

			criteria.add (
				Restrictions.ge (
					"_manualResponderRequest.timestamp",
					search.timestampAfter ()));

		}

		if (search.timestampBefore () != null) {

			criteria.add (
				Restrictions.lt (
					"_manualResponderRequest.timestamp",
					search.timestampBefore ()));

		}

		// set order

		switch (search.order ()) {

		case timestampDesc:

			criteria

				.addOrder (
					Order.desc (
						"timestamp"))

				.addOrder (
					Order.desc (
						"id"));

			break;

		default:

			throw new RuntimeException ();

		}

		// perform and return

		return findMany (
			Integer.class,

			criteria

				.setProjection (
					Projections.id ())

				.list ());

	}

}
