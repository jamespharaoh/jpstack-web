package wbs.smsapps.forwarder.hibernate;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import lombok.NonNull;
import wbs.framework.hibernate.HibernateDao;
import wbs.smsapps.forwarder.model.ForwarderMessageInDaoMethods;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec;
import wbs.smsapps.forwarder.model.ForwarderRec;

public
class ForwarderMessageInDaoHibernate
	extends HibernateDao
	implements ForwarderMessageInDaoMethods {

	@Override
	public
	ForwarderMessageInRec findNext (
			@NonNull Instant now,
			@NonNull ForwarderRec forwarder) {

		return findOneOrNull (
			"findNext (now, forwarder)",
			ForwarderMessageInRec.class,

			createCriteria (
				ForwarderMessageInRec.class,
				"_forwarderMessageIn")

			.add (
				Restrictions.eq (
					"_forwarderMessageIn.forwarder",
					forwarder))

			.add (
				Restrictions.eq (
					"_forwarderMessageIn.pending",
					true))

			.add (
				Restrictions.or (

				Restrictions.isNull (
					"_forwarderMessageIn.borrowedTime"),

				Restrictions.le (
					"_forwarderMessageIn.borrowedTime",
					now)

			))

			.addOrder (
				Order.asc (
					"_forwarderMessageIn.createdTime"))

			.setMaxResults (
				1)

		);

	}

	@Override
	public
	List <ForwarderMessageInRec> findNextLimit (
			@NonNull Instant now,
			@NonNull Long maxResults) {

		return findMany (
			"findNextLimit (now, maxResults)",
			ForwarderMessageInRec.class,

			createCriteria (
				ForwarderMessageInRec.class,
				"_forwarderMessageIn")

			.add (
				Restrictions.eq (
					"_forwarderMessageIn.sendQueue",
					true))

			.add (
				Restrictions.lt (
					"_forwarderMessageIn.retryTime",
					now))

			.setMaxResults (
				toJavaIntegerRequired (
					maxResults))

		);

	}

	@Override
	public
	List <ForwarderMessageInRec> findPendingLimit (
			@NonNull ForwarderRec forwarder,
			@NonNull Long maxResults) {

		return findMany (
			"findPendingLimit (forwarder, maxResults)",
			ForwarderMessageInRec.class,

			createCriteria (
				ForwarderMessageInRec.class,
				"_forwarderMessageIn")

			.add (
				Restrictions.eq (
					"_forwarderMessageIn.pending",
					true))

			.add (
				Restrictions.eq (
					"_forwarderMessageIn.forwarder",
					forwarder))

			.addOrder (
				Order.asc (
					"_forwarderMessageIn.id"))

			.setMaxResults (
				toJavaIntegerRequired (
					maxResults))

		);

	}

}
