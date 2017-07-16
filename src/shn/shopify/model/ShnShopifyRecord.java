package shn.shopify.model;

import org.joda.time.Instant;
import org.joda.time.ReadableInstant;

import wbs.framework.entity.record.Record;

public
interface ShnShopifyRecord <RecordType extends Record <RecordType>>
	extends Record <RecordType> {

	Long getShopifyId ();

	RecordType setShopifyId (
			Long shopifyId);

	Boolean getShopifyNeedsSync ();

	RecordType setShopifyNeedsSync (
			Boolean shopifyNeedsSync);

	Instant getShopifyUpdatedAt ();

	RecordType setShopifyUpdatedAt (
			ReadableInstant shopifyUpdatedAt);

}
