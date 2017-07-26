package shn.shopify.logic;

import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.string.StringUtils.objectToString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import shn.product.model.ShnProductObjectHelper;
import shn.product.model.ShnProductRec;
import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.apiclient.collect.ShopifyCollectApiClient;
import shn.shopify.apiclient.collect.ShopifyCollectListResponse;
import shn.shopify.apiclient.collect.ShopifyCollectRequest;
import shn.shopify.apiclient.collect.ShopifyCollectResponse;
import shn.shopify.model.ShnShopifyConnectionRec;

@SingletonComponent ("shnShopifyProductSubCategorySynchronisationHelper")
public
class ShnShopifyProductSubCategorySynchronisationHelper
	implements ShnShopifySynchronisationHelper <
		ShnProductRec,
		ShopifyCollectResponse
	> {

	// singleton dependecies

	@SingletonDependency
	ShopifyCollectApiClient collectApiClient;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ShnProductObjectHelper productHelper;

	@SingletonDependency
	ShnShopifyLogic shopifyLogic;

	// details

	@Override
	public
	String friendlyNameSingular () {
		return "product sub category";
	}

	@Override
	public
	String friendlyNamePlural () {
		return "product sub categories";
	}

	@Override
	public
	Long getShopifyId (
			@NonNull ShnProductRec localItem) {

		return localItem.getShopifySubCategoryCollectId ();

	}

	@Override
	public
	Boolean getShopifyNeedsSync (
			@NonNull ShnProductRec localItem) {

		return localItem.getShopifySubCategoryCollectNeedsSync ();

	}

	@Override
	public
	void setShopifyNeedsSync (
			@NonNull ShnProductRec localItem,
			@NonNull Boolean value) {

		localItem.setShopifySubCategoryCollectNeedsSync (
			value);

	}

	@Override
	public
	String eventCode (
			@NonNull EventType eventType) {

		switch (eventType) {

		case create:

			return stringFormat (
				"shopping_nation_product_sub_category_created_in_shopify");

		case update:

			return stringFormat (
				"shopping_nation_product_sub_category_updated_in_shopify");

		case remove:

			return stringFormat (
				"shopping_nation_product_sub_category_removed_in_shopify");

		default:

			throw shouldNeverHappen ();

		}

	}

	// public implementation

	@Override
	public
	List <ShnProductRec> findLocalItems (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findLocalItems");

		) {

			return productHelper.findNotDeleted (
				transaction);

		}

	}

	@Override
	public
	List <ShopifyCollectResponse> findRemoteItems (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findRemoteItems");

		) {

			ShopifyCollectListResponse response =
				collectApiClient.listAll (
					transaction,
					credentials);

			return response.collects ();

		}

	}

	@Override
	public
	void removeItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull Long id) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"removeItem");

		) {

			collectApiClient.remove (
				transaction,
				credentials,
				id);

		}

	}

	@Override
	public
	ShopifyCollectResponse createItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull ShnProductRec localProduct) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createItem");

		) {

			return collectApiClient.create (
				transaction,
				credentials,
				productSubCollectionRequest (
					transaction,
					connection,
					localProduct));

		}

	}

	@Override
	public
	ShopifyCollectResponse updateItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull ShnProductRec localItem,
			@NonNull ShopifyCollectResponse remoteItem) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"updateItem");

		) {

			return collectApiClient.update (
				transaction,
				credentials,
				productSubCollectionRequest (
					transaction,
					connection,
					localItem));

		}

	}

	@Override
	public
	List <String> compareItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull ShnProductRec localProduct,
			@NonNull ShopifyCollectResponse remoteCollect) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"compareItem");

		) {

			return ImmutableList.<String> builder ()

				.addAll (
					shopifyLogic.compareAttributes (
						transaction,
						connection,
						productSubCategoryAttributes,
						localProduct,
						remoteCollect))

				.build ()

			;

		}

	}

	// private implementation

	private
	ShopifyCollectRequest productSubCollectionRequest (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull ShnProductRec localProduct) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"productRequest");

		) {

			return shopifyLogic.createRequest (
				transaction,
				connection,
				productSubCategoryAttributes,
				localProduct,
				ShopifyCollectRequest.class);

		}

	}

	@Override
	public
	void saveShopifyData (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductRec localProduct,
			@NonNull ShopifyCollectResponse remoteCollect) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"saveShopifyData");

		) {

			localProduct

				.setShopifySubCategoryCollectId (
					remoteCollect.id ())

				.setShopifySubCategoryCollectCreatedAt (
					Instant.parse (
						remoteCollect.createdAt ()))

				.setShopifySubCategoryCollectUpdatedAt (
					Instant.parse (
						remoteCollect.updatedAt ()))

			;

		}

	}

	// data

	ShopifySynchronisationAttribute.Factory <
		ShnProductRec,
		ShopifyCollectRequest,
		ShopifyCollectResponse
	> productSubCategoryAttributeFactory =
		new ShopifySynchronisationAttribute.Factory<> ();

	List <ShopifySynchronisationAttribute <
		ShnProductRec,
		ShopifyCollectRequest,
		ShopifyCollectResponse
	>> productSubCategoryAttributes =
		ImmutableList.of (

		// general

		productSubCategoryAttributeFactory.remoteIdSimple (
			Long.class,
			"shopify id",
			ShopifyCollectResponse::id,
			ShnProductRec::setShopifySubCategoryCollectId,
			ShnProductRec::getShopifySubCategoryCollectId,
			ShopifyCollectRequest::id),

		productSubCategoryAttributeFactory.sendSimple (
			Long.class,
			"product id",
			ShnProductRec::getShopifyId,
			ShopifyCollectRequest::productId,
			ShopifyCollectResponse::productId),

		productSubCategoryAttributeFactory.sendSimple (
			Long.class,
			"collection id",
			localProduct ->
				localProduct.getSubCategory ().getShopifyId (),
			ShopifyCollectRequest::collectionId,
			ShopifyCollectResponse::collectionId)

	);

}
