package wbs.platform.priv.model;

import java.util.List;

public
interface PrivTypeDaoMethods {

	PrivTypeRec findRequired (
			Long id);

	List<PrivTypeRec> findAll ();

}