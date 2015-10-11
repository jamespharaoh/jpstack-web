package wbs.platform.object.core.model;

import java.util.List;

public
interface ObjectTypeDaoMethods {

	ObjectTypeRec findById (
			int id);

	ObjectTypeRec findByCode (
			String code);

	List<ObjectTypeRec> findAll ();

}