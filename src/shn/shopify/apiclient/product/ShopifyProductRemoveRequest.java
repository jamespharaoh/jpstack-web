package shn.shopify.apiclient.product;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataClass;

import shn.shopify.apiclient.ShopifyApiClientCredentials;

@Accessors (fluent = true)
@Data
@DataClass
public
class ShopifyProductRemoveRequest {

	ShopifyApiClientCredentials credentials;

	Long id;

}
