package shn.product.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface ShnProductVariantDaoMethods {

	List <Long> searchIds (
			Transaction parentTransaction,
			ShnProductVariantSearch shnProductSearch);

}
