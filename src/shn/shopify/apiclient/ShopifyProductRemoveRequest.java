package shn.shopify.apiclient;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class ShopifyProductRemoveRequest {

	ShopifyApiClientCredentials credentials;

	Long id;

}
