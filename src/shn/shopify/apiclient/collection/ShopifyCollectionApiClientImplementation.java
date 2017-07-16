package shn.shopify.apiclient.collection;

import static wbs.utils.etc.Misc.todo;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.logging.TaskLogger;

import shn.shopify.apiclient.ShopifyApiClientCredentials;

@SingletonComponent ("shopifyCollectionApiClient")
public
class ShopifyCollectionApiClientImplementation
	implements ShopifyCollectionApiClient {

	@Override
	public
	ShopifyCollectionListResponse listAllCollections (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials) {

		throw todo ();

	}

	@Override
	public
	ShopifyCollectionResponse createCollection (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShopifyCollectionRequest collection) {

		throw todo ();

	}

	@Override
	public
	void removeCollection (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull Long id) {

		throw todo ();

	}

}
