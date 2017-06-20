package shn.shopify.apiclient;

import static wbs.utils.collection.CollectionUtils.emptyList;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class ShopifyProductRequest {

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

	@DataChildren (
		childElement = "images")
	List <ShopifyProductImageResponse> images =
		emptyList ();

}
