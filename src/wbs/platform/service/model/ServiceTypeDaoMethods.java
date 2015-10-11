package wbs.platform.service.model;

import java.util.List;

import wbs.platform.object.core.model.ObjectTypeRec;

public
interface ServiceTypeDaoMethods {

	List<ServiceTypeRec> findByParentObjectType (
			ObjectTypeRec parentObjectType);

}