package shn.shopify.logic;

import wbs.framework.database.Transaction;

import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.model.ShnShopifyStoreRec;

public
interface ShnShopifyLogic {

	ShopifyApiClientCredentials getApiCredentials (
			Transaction parentTransaction,
			ShnShopifyStoreRec shopifyStore);

}
