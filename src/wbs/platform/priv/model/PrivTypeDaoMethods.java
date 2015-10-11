package wbs.platform.priv.model;

import java.util.List;

import wbs.platform.object.core.model.ObjectTypeRec;

public
interface PrivTypeDaoMethods {

	List<PrivTypeRec> findByParentObjectType (
			ObjectTypeRec parentObjectType);

}