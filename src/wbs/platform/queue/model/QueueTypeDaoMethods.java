package wbs.platform.queue.model;

import java.util.List;

import wbs.platform.object.core.model.ObjectTypeRec;

public
interface QueueTypeDaoMethods {

	List<QueueTypeRec> findByParentObjectType (
			ObjectTypeRec parentType);

}