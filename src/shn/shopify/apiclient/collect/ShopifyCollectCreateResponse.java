package shn.shopify.apiclient.collect;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataChild;

import shn.shopify.apiclient.ShopifyApiResponse;

@Accessors (fluent = true)
@Data
public
class ShopifyCollectCreateResponse
	implements ShopifyApiResponse {

	@DataChild (
		name = "collect")
	ShopifyCollectResponse collect;

}
