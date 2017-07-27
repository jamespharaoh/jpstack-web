package shn.shopify.logic;

import java.util.List;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectHelper;

import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.apiclient.ShopifyApiRequestItem;
import shn.shopify.apiclient.ShopifyApiResponseItem;
import shn.shopify.model.ShnShopifyConnectionRec;

public
interface ShnShopifySynchronisationHelper <
	Local extends Record <Local>,
	Request extends ShopifyApiRequestItem,
	Response extends ShopifyApiResponseItem
> {

	ObjectHelper <Local> objectHelper ();

	String friendlyNameSingular ();
	String friendlyNamePlural ();

	String eventCode (
			EventType eventType);

	Long getShopifyId (
			Local localItem);

	Boolean getShopifyNeedsSync (
			Local localItem);

	void setShopifyNeedsSync (
			Local localItem,
			Boolean value);

	Optional <Local> findLocalItem (
			Transaction parentTransaction,
			Long id);

	List <Local> findLocalItems (
			Transaction parentTransaction);

	List <Response> findRemoteItems (
			Transaction parentTransaction,
			ShopifyApiClientCredentials credentials);

	Request localToRequest (
			Transaction parentTransaction,
			ShnShopifyConnectionRec connection,
			Local localItem);

	void removeItem (
			Transaction parentTransaction,
			ShopifyApiClientCredentials credentials,
			ShnShopifyConnectionRec connection,
			Long id);

	Response createRemoteItem (
			Transaction parentTransaction,
			ShopifyApiClientCredentials credentials,
			Request request);

	Response updateItem (
			Transaction parentTransaction,
			ShopifyApiClientCredentials credentials,
			Request request);

	List <String> compareItem (
			Transaction parentTransaction,
			ShnShopifyConnectionRec connection,
			Local localItem,
			Response remoteItem);

	void updateLocalItem (
			Transaction parentTransaction,
			Local localItem,
			Response remoteItem);

	static
	enum EventType {
		create,
		update,
		remove;
	}

}
