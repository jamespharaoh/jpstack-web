package shn.show.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface ShnShowDaoMethods {

	List <Long> searchIds (
			Transaction parentTransaction,
			ShnShowSearch shnShowSearch);

}
