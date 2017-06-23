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
class ShopifyProductResponse {

	@DataAttribute (
		name = "id")
	Long id;

	@DataAttribute (
		name = "title")
	String title;

	@DataAttribute (
		name = "updated_at")
	String updatedAt;

	@DataChildren (
		childElement = "images")
	List <ShopifyProductImageResponse> images =
		emptyList ();

}
