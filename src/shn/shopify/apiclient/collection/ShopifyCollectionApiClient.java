package shn.shopify.apiclient.collection;

import wbs.framework.logging.TaskLogger;

import shn.shopify.apiclient.ShopifyApiClientCredentials;

public
interface ShopifyCollectionApiClient {

	ShopifyCollectionListResponse listAllCollections (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials);

	ShopifyCollectionResponse createCollection (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials,
			ShopifyCollectionRequest collection);

	void removeCollection (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials,
			Long id);

}
