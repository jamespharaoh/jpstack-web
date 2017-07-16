package shn.shopify.apiclient.product;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.framework.data.annotations.DataAttribute;

@Accessors (fluent = true)
@Data
public
class ShopifyProductImageResponse {

	@DataAttribute (
		name = "id")
	Long id;

	@DataAttribute (
		name = "product_id")
	Long productId;

	@DataAttribute (
		name = "position")
	Long position;

	@DataAttribute (
		name = "created_at")
	Instant createdAt;

	@DataAttribute (
		name = "updated_at")
	Instant updatedAt;

	@DataAttribute (
		name = "src")
	String src;

	@DataAttribute (
		name = "variant_ids")
	List <Long> variantIds;

}
