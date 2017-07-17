package shn.shopify.apiclient.product;

import static wbs.utils.collection.CollectionUtils.emptyList;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;

import shn.shopify.apiclient.ShopifyApiResponseItem;

@Accessors (fluent = true)
@Data
public
class ShopifyProductResponse
	implements ShopifyApiResponseItem {

	@DataAttribute (
		name = "id")
	Long id;

	@DataAttribute (
		name = "title")
	String title;

	@DataAttribute (
		name = "body_html")
	String bodyHtml;

	@DataAttribute (
		name = "vendor")
	String vendor;

	@DataAttribute (
		name = "product_type")
	String productType;

	@DataAttribute (
		name = "updated_at")
	String updatedAt;

	@DataChildren (
		childrenElement = "images")
	List <ShopifyProductImageResponse> images =
		emptyList ();

	@DataChildren (
		childrenElement = "variants")
	List <ShopifyProductVariantResponse> variants =
		emptyList ();

}
