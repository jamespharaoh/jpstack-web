package wbs.platform.user.model;

import wbs.platform.priv.model.PrivRec;

public
interface UserPrivDaoMethods {

	UserPrivRec find (
			UserRec user,
			PrivRec priv);

}