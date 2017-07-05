package shn.product.api;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;

import shn.core.model.ShnDatabaseObjectHelper;
import shn.core.model.ShnDatabaseRec;
import shn.product.model.ShnProductImageObjectHelper;
import shn.product.model.ShnProductImageRec;
import shn.product.model.ShnProductObjectHelper;
import shn.product.model.ShnProductRec;
import wbs.web.context.RequestContext;
import wbs.web.exceptions.HttpNotFoundException;
import wbs.web.responder.BinaryResponder;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("shnProductImageOriginalGetApiAction")
public
class ShnProductImageOriginalGetApiAction
	implements ApiAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	ShnDatabaseObjectHelper shnDatabaseHelper;

	@SingletonDependency
	ShnProductObjectHelper shnProductHelper;

	@SingletonDependency
	ShnProductImageObjectHelper shnProductImageHelper;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <BinaryResponder> binaryResponderProvider;

	// public implementation

	@Override
	public
	Optional <WebResponder> handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"handle");

		) {

			// lookup database

			Optional <ShnDatabaseRec> shnDatabaseOptional =
				shnDatabaseHelper.find (
					transaction,
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"shn-database-id")));

			if (
				optionalIsNotPresent (
					shnDatabaseOptional)
			) {
				throw new HttpNotFoundException ();
			}

			ShnDatabaseRec shnDatabase =
				optionalGetRequired (
					shnDatabaseOptional);

			// lookup product

			Optional <ShnProductRec> productOptional =
				shnProductHelper.find (
					transaction,
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"shn-product-id")));

			if (
				optionalIsNotPresent (
					productOptional)
			) {
				throw new HttpNotFoundException ();
			}

			ShnProductRec product =
				optionalGetRequired (
					productOptional);

			if (
				referenceNotEqualWithClass (
					ShnDatabaseRec.class,
					shnDatabase,
					product.getDatabase ())
			) {
				throw new HttpNotFoundException ();
			}

			// lookup product image

			Optional <ShnProductImageRec> productImageOptional =
				shnProductImageHelper.find (
					transaction,
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"shn-product-image-id")));

			if (
				optionalIsNotPresent (
					productImageOptional)
			) {
				throw new HttpNotFoundException ();
			}

			ShnProductImageRec productImage =
				optionalGetRequired (
					productImageOptional);

			if (
				referenceNotEqualWithClass (
					ShnProductRec.class,
					product,
					productImage.getProduct ())
			) {
				throw new HttpNotFoundException ();
			}

			// return responder

			MediaRec originalMedia =
				productImage.getOriginalMedia ();

			return optionalOf (
				binaryResponderProvider.provide (
					transaction)

				.contentType (
					originalMedia.getMediaType ().getMimeType ())

				.data (
					originalMedia.getContent ().getData ())

			);

		}

	}

}
