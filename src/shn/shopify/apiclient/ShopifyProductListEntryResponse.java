package shn.shopify.apiclient;

import static wbs.utils.collection.CollectionUtils.emptyList;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;

@Accessors (fluent = true)
@Data
public
class ShopifyProductListEntryResponse {

	@DataAttribute (
		name = "id")
	Long id;

	@DataAttribute (
		name = "title")
	String title;

	@DataChildren (
		childElement = "images")
	List <ShopifyProductImageResponse> images =
		emptyList ();

}
