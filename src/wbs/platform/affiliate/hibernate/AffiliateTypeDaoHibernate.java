package wbs.platform.affiliate.hibernate;

import java.util.List;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.affiliate.model.AffiliateTypeDao;
import wbs.platform.affiliate.model.AffiliateTypeRec;
import wbs.platform.object.core.model.ObjectTypeRec;

public
class AffiliateTypeDaoHibernate
	extends HibernateDao
	implements AffiliateTypeDao {

	@Override
	public
	List<AffiliateTypeRec> findByParentObjectType (
			ObjectTypeRec parentType) {

		return findMany (
			AffiliateTypeRec.class,

			createQuery (
				"FROM AffiliateTypeRec affiliateType " +
				"WHERE affiliateType.parentType = :parentType")

			.setEntity (
				"parentType",
				parentType)

			.list ());

	}

}
