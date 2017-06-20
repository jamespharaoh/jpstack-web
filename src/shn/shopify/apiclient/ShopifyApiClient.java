package shn.shopify.apiclient;

import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import shn.shopify.model.ShnShopifyStoreRec;

public
interface ShopifyApiClient {

	ShopifyApiClientCredentials getCredentials (
			Transaction parentTransaction,
			ShnShopifyStoreRec shopifyStore);

	ShopifyProductListResponse listAllProducts (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials);

}
