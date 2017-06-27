package shn.shopify.apiclient;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class ShopifyProductVariantRequest {

	@DataAttribute (
		name = "id")
	Long id;

	@DataAttribute (
		name = "title")
	String title;

	// pricing

	@DataAttribute (
		name = "price")
	Double price;

	@DataAttribute (
		name = "compare_at_price")
	Double compareAtPrice;

	// options

	@DataAttribute (
		name = "option1")
	String option1;

	@DataAttribute (
		name = "option2")
	String option2;

	@DataAttribute (
		name = "option3")
	String option3;

	@DataAttribute (
		name = "option4")
	String option4;

	@DataAttribute (
		name = "option5")
	String option5;

	@DataAttribute (
		name = "option6")
	String option6;

	@DataAttribute (
		name = "option7")
	String option7;

	@DataAttribute (
		name = "option8")
	String option8;

}
