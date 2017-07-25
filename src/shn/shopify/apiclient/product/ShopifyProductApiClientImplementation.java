package shn.shopify.apiclient.product;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.apiclient.GenericHttpSender;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.apiclient.ShopifyApiRequest;
import shn.shopify.apiclient.ShopifyApiResponse;

@SingletonComponent ("shopifyProductApiClient")
public
class ShopifyProductApiClientImplementation
	implements ShopifyProductApiClient {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("shopifyHttpSender")
	ComponentProvider <GenericHttpSender <
		ShopifyApiRequest,
		ShopifyApiResponse
	>> shopifyHttpSenderProvider;

	// public implementation

	@Override
	public
	ShopifyProductListResponse listAllProducts (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"listProducts")

		) {

			ImmutableList.Builder <ShopifyProductResponse> builder =
				ImmutableList.builder ();

			for (
				long page = 0l;
				true;
				page ++
			) {

				ShopifyProductListResponse response =
					genericCastUnchecked (
						shopifyHttpSenderProvider.provide (
							taskLogger)

					.allInOne (
						taskLogger,
						new ShopifyProductListRequest ()

					.httpCredentials (
						credentials)

					.limit (
						250l)

					.page (
						page)

				));

				builder.addAll (
					response.products ());

				if (
					lessThan (
						collectionSize (
							response.products ()),
						250l)
				) {
					break;
				}

			}

			return new ShopifyProductListResponse ()

				.products (
					builder.build ())

			;

		}

	}

	@Override
	public
	ShopifyProductResponse createProduct (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShopifyProductRequest product) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createProduct");

		) {

			ShopifyProductCreateResponse response =
				genericCastUnchecked (
					shopifyHttpSenderProvider.provide (
						taskLogger)

				.allInOne (
					taskLogger,
					new ShopifyProductCreateRequest ()

					.httpCredentials (
						credentials)

					.product (
						product)

				)

			);

			return response.product ();

		}

	}

	@Override
	public
	ShopifyProductResponse updateProduct (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShopifyProductRequest product) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createProduct");

		) {

			ShopifyProductUpdateResponse response =
				genericCastUnchecked (
					shopifyHttpSenderProvider.provide (
						taskLogger)

				.allInOne (
					taskLogger,
					new ShopifyProductUpdateRequest ()

				.httpCredentials (
					credentials)

				.product (
					product)

			));

			return response.product ();

		}

	}

	@Override
	public
	void removeProduct (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull Long id) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"removeProduct");

		) {

			shopifyHttpSenderProvider.provide (
				taskLogger)

				.allInOne (
					taskLogger,
					new ShopifyProductRemoveRequest ()

					.httpCredentials (
						credentials)

					.id (
						id)

				)

			;

		}

	}

}
