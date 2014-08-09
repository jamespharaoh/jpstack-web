package wbs.psychic.user.core.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.psychic.core.model.PsychicRec;
import wbs.psychic.user.core.model.PsychicUserDao;
import wbs.psychic.user.core.model.PsychicUserRec;
import wbs.psychic.user.core.model.PsychicUserSearch;
import wbs.sms.number.core.model.NumberRec;

@SingletonComponent ("psychicUserDao")
public
class PsychicUserDaoHibernate
	extends HibernateDao
	implements PsychicUserDao {

	@Override
	public
	PsychicUserRec find (
			PsychicRec psychic,
			NumberRec number) {

		return findOne (
			PsychicUserRec.class,

			createQuery (
				"FROM PsychicUserRec user " +
				"WHERE user.psychic = :psychic " +
					"AND user.number = :number")

			.setEntity (
				"psychic",
				psychic)

			.setEntity (
				"number",
				number)

			.list ());

	}

	@Override
	public
	List<PsychicUserRec> find (
			NumberRec number) {

		return findMany (
			PsychicUserRec.class,

			createQuery (
				"FROM PsychicUserRec user " +
				"WHERE user.number = :number")

			.setEntity (
				"number",
				number)

			.list ());

	}

	@Override
	public
	List<Integer> searchIds (
			PsychicUserSearch search) {

		Criteria criteria =
			createCriteria (
				PsychicUserRec.class);

		Criteria numberCriteria =
			criteria.createCriteria (
				"number");

		if (search.getId () != null) {

			criteria.add (
				Restrictions.eq (
					"id",
					search.getId ()));

		}

		if (search.getCode () != null) {

			criteria.add (
				Restrictions.like (
					"code",
					search.getCode ()));

		}

		if (search.getNumber () != null) {

			numberCriteria.add (
				Restrictions.like (
					"number",
					search.getNumber ()));

		}

		criteria.setProjection (
			Projections.id ());

		return findMany (
			Integer.class,
			criteria.list ());

	}

}
