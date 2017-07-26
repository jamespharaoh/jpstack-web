package shn.shopify.apiclient.metafield;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataClass;

import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.apiclient.ShopifyApiRequest;
import shn.shopify.apiclient.ShopifyApiResponse;
import wbs.web.misc.HttpMethod;

@Accessors (fluent = true)
@Data
@DataClass
public
class ShopifyMetafieldCreateRequest
	implements ShopifyApiRequest {

	ShopifyApiClientCredentials httpCredentials;

	@DataChild (
		name = "metafield")
	ShopifyMetafieldRequest metafield;

	// shopify api request implementation

	@Override
	public
	HttpMethod httpMethod () {
		return HttpMethod.post;
	}

	@Override
	public
	String httpPath () {
		return "/admin/metafields.json";
	}

	@Override
	public
	Class <? extends ShopifyApiResponse> httpResponseClass () {
		return ShopifyMetafieldCreateResponse.class;
	}

}
