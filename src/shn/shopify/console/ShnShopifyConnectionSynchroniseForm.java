package shn.shopify.console;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class ShnShopifyConnectionSynchroniseForm {

	Boolean createProducts;
	Boolean updateProducts;
	Boolean removeProducts;

	Long maxOperations;

}
