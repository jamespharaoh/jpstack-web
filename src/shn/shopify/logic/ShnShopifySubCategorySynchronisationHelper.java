package shn.shopify.logic;

import static wbs.utils.etc.Misc.shouldNeverHappen;
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

import shn.product.model.ShnProductSubCategoryObjectHelper;
import shn.product.model.ShnProductSubCategoryRec;
import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.apiclient.customcollection.ShopifyCustomCollectionApiClient;
import shn.shopify.apiclient.customcollection.ShopifyCustomCollectionListResponse;
import shn.shopify.apiclient.customcollection.ShopifyCustomCollectionRequest;
import shn.shopify.apiclient.customcollection.ShopifyCustomCollectionResponse;
import shn.shopify.model.ShnShopifyConnectionRec;

@SingletonComponent ("shnShopifySubCategorySynchronisationHelper")
public
class ShnShopifySubCategorySynchronisationHelper
	implements ShnShopifySynchronisationHelper <
		ShnProductSubCategoryRec,
		ShopifyCustomCollectionResponse
	> {

	// singleton dependecies

	@SingletonDependency
	ShopifyCustomCollectionApiClient collectionApiClient;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ShnProductSubCategoryObjectHelper subCategoryHelper;

	@SingletonDependency
	ShnShopifyLogic shopifyLogic;

	// details

	@Override
	public
	String friendlyNameSingular () {
		return "sub category";
	}

	@Override
	public
	String friendlyNamePlural () {
		return "sub categories";
	}

	@Override
	public
	String eventCode (
			@NonNull EventType eventType) {

		switch (eventType) {

		case create:

			return stringFormat (
				"shopping_nation_sub_category_created_in_shopify");

		case update:

			return stringFormat (
				"shopping_nation_sub_category_updated_in_shopify");

		case remove:

			return stringFormat (
				"shopping_nation_sub_category_removed_in_shopify");

		default:

			throw shouldNeverHappen ();

		}

	}

	// public implementation

	@Override
	public
	List <ShnProductSubCategoryRec> findLocalItems (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findLocalItems");

		) {

			return subCategoryHelper.findNotDeleted (
				transaction);

		}

	}

	@Override
	public
	List <ShopifyCustomCollectionResponse> findRemoteItems (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findRemoteItems");

		) {

			ShopifyCustomCollectionListResponse shopifyCollectionListResponse =
				collectionApiClient.listAll (
					transaction,
					credentials);

			return shopifyCollectionListResponse.collections ();

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

			collectionApiClient.remove (
				transaction,
				credentials,
				id);

		}

	}

	@Override
	public
	ShopifyCustomCollectionResponse createItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull ShnProductSubCategoryRec localSubCategory) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createItem");

		) {

			return collectionApiClient.create (
				transaction,
				credentials,
				subCategoryRequest (
					transaction,
					connection,
					localSubCategory));

		}

	}

	@Override
	public
	ShopifyCustomCollectionResponse updateItem (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull ShnProductSubCategoryRec localItem,
			@NonNull ShopifyCustomCollectionResponse remoteItem) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"updateItem");

		) {

			return collectionApiClient.update (
				transaction,
				credentials,
				subCategoryRequest (
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
			@NonNull ShnProductSubCategoryRec localSubCategory,
			@NonNull ShopifyCustomCollectionResponse remoteCollection) {

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
						subCategoryAttributes,
						localSubCategory,
						remoteCollection))

				.build ()

			;

		}

	}

	// private implementation

	private
	ShopifyCustomCollectionRequest subCategoryRequest (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyConnectionRec connection,
			@NonNull ShnProductSubCategoryRec localSubCategory) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"subCategoryRequest");

		) {

			return shopifyLogic.createRequest (
				transaction,
				connection,
				subCategoryAttributes,
				localSubCategory,
				ShopifyCustomCollectionRequest.class);

		}

	}

	@Override
	public
	void saveShopifyData (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductSubCategoryRec localSubCategory,
			@NonNull ShopifyCustomCollectionResponse remoteCollection) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"saveShopifyData");

		) {

			localSubCategory

				.setShopifyId (
					remoteCollection.id ())

				.setShopifyUpdatedAt (
					Instant.parse (
						remoteCollection.updatedAt ()))

			;

		}

	}

	// data

	ShopifySynchronisationAttribute.Factory <
		ShnProductSubCategoryRec,
		ShopifyCustomCollectionRequest,
		ShopifyCustomCollectionResponse
	> subCategoryAttributeFactory =
		new ShopifySynchronisationAttribute.Factory<> ();

	List <ShopifySynchronisationAttribute <
		ShnProductSubCategoryRec,
		ShopifyCustomCollectionRequest,
		ShopifyCustomCollectionResponse
	>> subCategoryAttributes =
		ImmutableList.of (

		// general

		subCategoryAttributeFactory.remoteIdSimple (
			Long.class,
			"shopify id",
			ShopifyCustomCollectionResponse::id,
			ShnProductSubCategoryRec::setShopifyId,
			ShnProductSubCategoryRec::getShopifyId,
			ShopifyCustomCollectionRequest::id),

		subCategoryAttributeFactory.sendSimple (
			String.class,
			"title",
			ShnProductSubCategoryRec::getPublicTitle,
			ShopifyCustomCollectionRequest::title,
			ShopifyCustomCollectionResponse::title),

		subCategoryAttributeFactory.sendSimple (
			String.class,
			"body html",
			localSubCategory ->
				localSubCategory.getPublicDescription ().getText (),
			ShopifyCustomCollectionRequest::bodyHtml,
			ShopifyCustomCollectionResponse::bodyHtml),

		// miscellaneous

		subCategoryAttributeFactory.receiveSimple (
			Instant.class,
			"updated at",
			remoteVariant ->
				Instant.parse (
					remoteVariant.updatedAt ()),
			ShnProductSubCategoryRec::setShopifyUpdatedAt,
			ShnProductSubCategoryRec::getShopifyUpdatedAt)

	);

}
