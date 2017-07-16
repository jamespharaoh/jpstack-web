package shn.shopify.logic;

import java.util.List;

import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.apiclient.ShopifyApiResponse;

public
interface ShnShopifySynchronisationHelper <
	Local extends Record <Local>,
	Remote extends ShopifyApiResponse
> {

	String friendlyNameSingular ();

	String friendlyNamePlural ();

	List <Local> findLocalItems (
			Transaction parentTransaction);

	List <Remote> findRemoteItems (
			Transaction parentTransaction,
			ShopifyApiClientCredentials credentials);

	void removeItem (
			Transaction parentTransaction,
			ShopifyApiClientCredentials credentials,
			Long id);

	Remote createItem (
			NestedTransaction parentTransaction,
			ShopifyApiClientCredentials credentials,
			Local localItem);

	Remote updateItem (
			NestedTransaction parentTransaction,
			ShopifyApiClientCredentials credentials,
			Local localItem,
			Remote remoteItem);

}
