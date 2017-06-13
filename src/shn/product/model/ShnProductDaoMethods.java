package shn.product.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface ShnProductDaoMethods {

	List <Long> searchIds (
			Transaction parentTransaction,
			ShnProductSearch shnProductSearch);

}
