package shn.shopify.apiclient;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class ShopifyProductListRequest {

	ShopifyApiClientCredentials credentials;

	List <Long> ids;
	Long limit;
	Long page;

}
