package shn.shopify.apiclient.product;

import wbs.framework.logging.TaskLogger;

import shn.shopify.apiclient.ShopifyApiClientCredentials;

public
interface ShopifyProductApiClient {

	ShopifyProductListResponse listAllProducts (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials);

	ShopifyProductResponse createProduct (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials,
			ShopifyProductRequest product);

	void removeProduct (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials,
			Long id);

}
