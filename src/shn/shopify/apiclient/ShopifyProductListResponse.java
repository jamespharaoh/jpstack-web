package shn.shopify.apiclient;

import static wbs.utils.collection.CollectionUtils.emptyList;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataChildren;

@Accessors (fluent = true)
@Data
public
class ShopifyProductListResponse {

	@DataChildren (
		childrenElement = "products")
	List <ShopifyProductResponse> products =
		emptyList ();

}
