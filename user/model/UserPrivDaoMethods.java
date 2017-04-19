package wbs.platform.user.model;

import java.util.List;

import wbs.platform.priv.model.PrivRec;

public
interface UserPrivDaoMethods {

	UserPrivRec find (
			UserRec user,
			PrivRec priv);

	List <UserPrivRec> find (
			PrivRec priv);

}