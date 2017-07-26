package shn.shopify.apiclient.customcollection;

import wbs.framework.logging.TaskLogger;

import shn.shopify.apiclient.ShopifyApiClientCredentials;

public
interface ShopifyCustomCollectionApiClient {

	ShopifyCustomCollectionListResponse listAll (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials);

	ShopifyCustomCollectionResponse create (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials,
			ShopifyCustomCollectionRequest request);

	ShopifyCustomCollectionResponse update (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials,
			ShopifyCustomCollectionRequest request);

	void remove (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials,
			Long id);

}
