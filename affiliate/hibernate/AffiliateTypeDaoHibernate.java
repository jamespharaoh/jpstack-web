package wbs.platform.affiliate.hibernate;

import java.util.List;

import lombok.NonNull;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.affiliate.model.AffiliateTypeDao;
import wbs.platform.affiliate.model.AffiliateTypeRec;

public
class AffiliateTypeDaoHibernate
	extends HibernateDao
	implements AffiliateTypeDao {

	@Override
	public
	List<AffiliateTypeRec> findAll () {

		return findMany (
			"findAll ()",
			AffiliateTypeRec.class,

			createCriteria (
				AffiliateTypeRec.class)

		);

	}

	@Override
	public
	AffiliateTypeRec findRequired (
			@NonNull Long affiliateTypeId) {

		return get (
			AffiliateTypeRec.class,
			(int) (long) affiliateTypeId);

	}

}
