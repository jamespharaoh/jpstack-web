package shn.product.logic;

import java.util.Collection;
import java.util.List;

import shn.product.model.ShnProductVariantValueRec;

public
interface ShnProductLogic {

	List <ShnProductVariantValueRec> sortVariantValues (
			Collection <ShnProductVariantValueRec> variantValues);

}
