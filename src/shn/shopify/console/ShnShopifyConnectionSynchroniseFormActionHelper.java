package shn.shopify.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThan;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.formaction.ConsoleFormActionHelper;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import shn.product.model.ShnProductRec;
import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.apiclient.product.ShopifyProductResponse;
import shn.shopify.logic.ShnShopifyLogic;
import shn.shopify.logic.ShnShopifySynchronisation;
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
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ShnShopifyLogic shopifyLogic;

	@SingletonDependency
	ShnShopifyConnectionConsoleHelper shopifyConnectionHelper;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("shnShopifyProductSynchronisation")
	ComponentProvider <ShnShopifySynchronisation <
		?,
		ShnProductRec,
		ShopifyProductResponse
	>> productSynchronisationProvider;

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

				.createProducts (
					true)

				.updateProducts (
					true)

				.removeProducts (
					true)

				.maxOperations (
					10l)

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

			ShopifyApiClientCredentials shopifyCredentials =
				shopifyLogic.getApiCredentials (
					transaction,
					shopifyConnection.getStore ());

			ShnShopifySynchronisation <?, ?, ?> productionSynchronisation =
				productSynchronisationProvider.provide (
					transaction)

				.enableCreate (
					formState.createProducts ())

				.enableUpdate (
					formState.updateProducts ())

				.enableRemove (
					formState.removeProducts ())

				.maxOperations (
					formState.maxOperations ())

				.shopifyConnection (
					shopifyConnection)

				.shopifyCredentials (
					shopifyCredentials)

				.synchronise (
					transaction)

			;

			// commit and return

			transaction.commit ();

			if (
				moreThanZero (
					productionSynchronisation.numCreated ())
			) {

				requestContext.addNoticeFormat (
					"%s products created",
					integerToDecimalString (
						productionSynchronisation.numCreated ()));

			}

			if (
				moreThanZero (
					productionSynchronisation.numUpdated ())
			) {

				requestContext.addNoticeFormat (
					"%s products updated",
					integerToDecimalString (
						productionSynchronisation.numUpdated ()));

			}

			if (
				moreThanZero (
					productionSynchronisation.numRemoved ())
			) {

				requestContext.addNoticeFormat (
					"%s products removed",
					integerToDecimalString (
						productionSynchronisation.numRemoved ()));

			}

			if (
				moreThanZero (
					productionSynchronisation.numErrors ())
			) {

				requestContext.addErrorFormat (
					"%s data mismatch errors creating or updating products",
					integerToDecimalString (
						productionSynchronisation.numErrors ()));

			}

			if (
				moreThan (
					productionSynchronisation.numOperations (),
					formState.maxOperations ())
			) {

				requestContext.addWarningFormat (
					"Only performed %s out of %s operations, please repeat",
					integerToDecimalString (
						formState.maxOperations ()),
					integerToDecimalString (
						productionSynchronisation.numOperations ()));

			}

			return optionalAbsent ();

		}

	}

}
