package wbs.platform.affiliate.model;

import java.util.List;

public
interface AffiliateTypeDaoMethods {

	List<AffiliateTypeRec> findAll ();

	AffiliateTypeRec findRequired (
			Long affiliateTypeId);

}