package shn.shopify.apiclient;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;

@Accessors (fluent = true)
@Data
public
class ShopifyProductImageRequest {

	@DataAttribute (
		name = "src")
	String src;

}
