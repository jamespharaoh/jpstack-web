package wbs.platform.queue.model;

import java.util.List;

public
interface QueueTypeDaoMethods {

	QueueTypeRec findRequired (
			Long id);

	List<QueueTypeRec> findAll ();

}