package shn.shopify.apiclient.product;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import shn.shopify.apiclient.ShopifyApiClientCredentials;

@Accessors (fluent = true)
@Data
public
class ShopifyProductListRequest {

	ShopifyApiClientCredentials credentials;

	List <Long> ids;
	Long limit;
	Long page;

	List <String> fields;

}
