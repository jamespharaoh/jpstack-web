package wbs.platform.affiliate.model;

import java.util.List;

import wbs.platform.object.core.model.ObjectTypeRec;

public
interface AffiliateTypeDaoMethods {

	List<AffiliateTypeRec> findByParentObjectType (
			ObjectTypeRec parentObjectType);

}