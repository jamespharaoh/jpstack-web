package shn.shopify.logic;

import wbs.framework.database.Transaction;

import shn.product.model.ShnProductRec;
import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.model.ShnShopifyConnectionRec;
import shn.shopify.model.ShnShopifyStoreRec;

public
interface ShnShopifyLogic {

	ShopifyApiClientCredentials getApiCredentials (
			Transaction parentTransaction,
			ShnShopifyStoreRec shopifyStore);

	String productDescription (
			Transaction parentTransaction,
			ShnShopifyConnectionRec connection,
			ShnProductRec product);

}
