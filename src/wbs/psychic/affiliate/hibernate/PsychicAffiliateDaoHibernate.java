package wbs.psychic.affiliate.hibernate;

import java.util.List;

import wbs.framework.hibernate.HibernateDao;
import wbs.psychic.affiliate.model.PsychicAffiliateDao;
import wbs.psychic.affiliate.model.PsychicAffiliateRec;

public
class PsychicAffiliateDaoHibernate
	extends HibernateDao
	implements PsychicAffiliateDao {

	@Override
	public
	List<PsychicAffiliateRec> findByPsychic (
			int psychicId) {

		return findMany (
			PsychicAffiliateRec.class,

			createQuery (
				"FROM PsychicAffiliateRec psychicAffiliate " +
				"whERE psychicAffiliate.psychic.id = :psychicId")

			.setInteger (
				"psychicId",
				psychicId)

			.list ());

	}

}
