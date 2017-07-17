package shn.shopify.apiclient.product;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataChild;

import shn.shopify.apiclient.ShopifyApiResponse;

@Accessors (fluent = true)
@Data
public
class ShopifyProductCreateResponse
	implements ShopifyApiResponse {

	@DataChild (
		name = "product")
	ShopifyProductResponse product;

}
