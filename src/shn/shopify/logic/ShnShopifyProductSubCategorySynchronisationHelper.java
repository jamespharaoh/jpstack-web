package shn.shopify.logic;

import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHelper;

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
		ShopifyCollectRequest,
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
	ObjectHelper <ShnProductRec> objectHelper () {
		return productHelper;
	}

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
	Optional <ShnProductRec> findLocalItem (
			@NonNull Transaction parentTransaction,
			@NonNull Long id) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findLocalItem");

		) {

			Optional <ShnProductRec> productOptional =
				productHelper.find (
					transaction,
					id);

			if (
				optionalIsNotPresent (
					productOptional)
			) {
				return optionalAbsent ();
			}

			ShnProductRec product =
				optionalGetRequired (
					productOptional);

			if (product.getDeleted ()) {
				return optionalAbsent ();
			}

			return optionalOf (
				product);

		}

	}

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
	ShopifyCollectRequest localToRequest (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull ShnProductRec localProduct) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"localToRequest");

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
	ShopifyCollectResponse createRemoteItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShopifyCollectRequest request) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createItem");

		) {

			return collectApiClient.create (
				transaction,
				credentials,
				request);

		}

	}

	@Override
	public
	ShopifyCollectResponse updateItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShopifyCollectRequest request) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"updateItem");

		) {

			return collectApiClient.update (
				transaction,
				credentials,
				request);

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

	@Override
	public
	void updateLocalItem (
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
