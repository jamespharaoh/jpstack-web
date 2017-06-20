package shn.shopify.console;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThan;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.formaction.ConsoleFormActionHelper;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import shn.product.console.ShnProductConsoleHelper;
import shn.product.model.ShnProductRec;
import shn.shopify.apiclient.ShopifyApiClient;
import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.apiclient.ShopifyProductListResponse;
import shn.shopify.apiclient.ShopifyProductRequest;
import shn.shopify.apiclient.ShopifyProductResponse;
import shn.shopify.model.ShnShopifyConnectionRec;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("shnShopifyConnectionSynchroniseFormActionHelper")
public
class ShnShopifyConnectionSynchroniseFormActionHelper
	implements ConsoleFormActionHelper <
		ShnShopifyConnectionSynchroniseForm,
		Object
	> {

	// singleton depdendencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ShnProductConsoleHelper productHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ShopifyApiClient shopifyApiClient;

	@SingletonDependency
	ShnShopifyConnectionConsoleHelper shopifyConnectionHelper;

	// public implementation

	@Override
	public
	Permissions canBePerformed (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"canBePerformed");

		) {

			ShnShopifyConnectionRec shopifyConnection =
				shopifyConnectionHelper.findFromContextRequired (
					transaction);

			boolean allowed =
				privChecker.canRecursive (
					transaction,
					shopifyConnection,
					"admin");

			return new Permissions ()

				.canView (
					allowed)

				.canPerform (
					allowed)

			;

		}

	}

	@Override
	public
	void writePreamble (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Boolean submit) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"writePreamble");

		) {

			htmlParagraphWriteFormat (
				formatWriter,
				"Use this page to synchronise the platform database with ",
				"Shopify. Select the type of updates you wish to perform.");

		}

	}

	@Override
	public
	ShnShopifyConnectionSynchroniseForm constructFormState (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"constructFormState");

		) {

			return new ShnShopifyConnectionSynchroniseForm ()

				.updateProducts (
					true)

				.removeProducts (
					true)

			;

		}

	}

	@Override
	public
	Optional <WebResponder> processFormSubmission (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyConnectionSynchroniseForm formState) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"processFormSubmission");

		) {

			ShnShopifyConnectionRec shopifyConnection =
				shopifyConnectionHelper.findFromContextRequired (
					transaction);

			long productsCreated = 0l;
			long productsUpdated = 0l;
			long productsRemoved = 0l;

			long operations = 0;

			ShopifyApiClientCredentials credentials =
				shopifyApiClient.getCredentials (
					transaction,
					shopifyConnection.getStore ());

			// create or update products

			List <ShnProductRec> allProducts =
				productHelper.findAll (
					transaction);

			for (
				ShnProductRec product
					: allProducts
			) {

				if (
					isNull (
						product.getShopifyId ())
				) {

					operations ++;

					if (operations > maxOperations) {
						continue;
					}

					ShopifyProductResponse shopifyProduct =
						shopifyApiClient.createProduct (
							transaction,
							credentials,
							new ShopifyProductRequest ()

						.title (
							product.getPublicTitle ())

						.bodyHtml (
							product.getPublicDescription ().getText ())

						.vendor (
							product.getSupplier ().getPublicName ())

						.productType (
							product.getSubCategory ().getPublicName ())

					);

					product

						.setShopifyId (
							shopifyProduct.id ())

						.setShopifyCreateTime (
							transaction.now ())

					;

					productsCreated ++;

				}

			}

			// remove products

			if (formState.removeProducts ()) {

				ShopifyProductListResponse response =
					shopifyApiClient.listAllProducts (
						transaction,
						credentials);

				requestContext.addNoticeFormat (
					"Got %s products",
					integerToDecimalString (
						collectionSize (
							response.products ())));

			}

			// commit and return

			transaction.commit ();

			if (
				moreThanZero (
					productsCreated)
			) {

				requestContext.addNoticeFormat (
					"%s products created",
					integerToDecimalString (
						productsCreated));

			}

			if (
				moreThanZero (
					productsUpdated)
			) {

				requestContext.addNoticeFormat (
					"%s products updated",
					integerToDecimalString (
						productsUpdated));

			}

			if (
				moreThanZero (
					productsRemoved)
			) {

				requestContext.addNoticeFormat (
					"%s products removed",
					integerToDecimalString (
						productsRemoved));

			}

			if (
				moreThan (
					operations,
					maxOperations)
			) {

				requestContext.addWarningFormat (
					"Only performed %s out of %s operations, please repeat",
					integerToDecimalString (
						maxOperations),
					integerToDecimalString (
						operations));

			}

			return optionalAbsent ();

		}

	}

	// data

	public final static
	long maxOperations = 20;

}
