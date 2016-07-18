package wbs.platform.service.model;

import java.util.List;

public
interface ServiceTypeDaoMethods {

	ServiceTypeRec findRequired (
			Long id);

	List<ServiceTypeRec> findAll ();

}