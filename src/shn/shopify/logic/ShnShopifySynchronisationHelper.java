package shn.shopify.logic;

import java.util.List;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.apiclient.ShopifyApiResponseItem;
import shn.shopify.model.ShnShopifyConnectionRec;

public
interface ShnShopifySynchronisationHelper <
	Local extends Record <Local>,
	Remote extends ShopifyApiResponseItem
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
			ShnShopifyConnectionRec connection,
			Long id);

	Remote createItem (
			Transaction parentTransaction,
			ShopifyApiClientCredentials credentials,
			ShnShopifyConnectionRec connection,
			Local localItem);

	Remote updateItem (
			Transaction parentTransaction,
			ShopifyApiClientCredentials credentials,
			ShnShopifyConnectionRec connection,
			Local localItem,
			Remote remoteItem);

	boolean compareItem (
			Transaction parentTransaction,
			ShnShopifyConnectionRec connection,
			Local localItem,
			Remote remoteItem);

	void saveShopifyData (
			Transaction parentTransaction,
			Local localItem,
			Remote remoteItem);

}
