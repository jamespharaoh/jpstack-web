package shn.shopify.console;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class ShnShopifyConnectionSynchroniseForm {

	Boolean create;
	Boolean update;
	Boolean remove;

	Long maxOperations;

}
