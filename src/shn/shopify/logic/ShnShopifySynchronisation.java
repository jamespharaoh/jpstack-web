package shn.shopify.logic;

import wbs.framework.database.Transaction;

import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.apiclient.ShopifyApiResponseItem;
import shn.shopify.model.ShnShopifyConnectionRec;
import shn.shopify.model.ShnShopifyRecord;

public
interface ShnShopifySynchronisation <
	Self extends ShnShopifySynchronisation <Self, Local, Remote>,
	Local extends ShnShopifyRecord <Local>,
	Remote extends ShopifyApiResponseItem
> {

	Self enableCreate (
			Boolean enableCreate);

	Self enableUpdate (
			Boolean enableUpdate);

	Self enableRemove (
			Boolean enableRemove);

	Self maxOperations (
			Long maxOperations);

	Self shopifyConnection (
			ShnShopifyConnectionRec shopifyConnection);

	Self shopifyCredentials (
			ShopifyApiClientCredentials shopifyCredentials);

	Self synchronise (
			Transaction parentTransaction);

	long numCreated ();
	long numUpdated ();
	long numRemoved ();

	long numNotCreated ();
	long numNotUpdated ();
	long numNotRemoved ();

	long numErrors ();

	long numOperations ();

}
