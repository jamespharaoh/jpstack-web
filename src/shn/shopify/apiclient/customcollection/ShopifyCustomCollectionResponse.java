package shn.shopify.apiclient.customcollection;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChild;

import shn.shopify.apiclient.ShopifyApiResponseItem;

@Accessors (fluent = true)
@Data
public
class ShopifyCustomCollectionResponse
	implements ShopifyApiResponseItem {

	@DataAttribute (
		name = "id")
	Long id;

	@DataAttribute (
		name = "handle")
	String handle;

	@DataAttribute (
		name = "title")
	String title;

	@DataAttribute (
		name = "body_html")
	String bodyHtml;

	@DataChild (
		name = "image")
	ShopifyCustomCollectionImageResponse image;

	@DataAttribute (
		name = "updated_at")
	String updatedAt;

}
