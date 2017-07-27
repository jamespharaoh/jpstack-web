package shn.product.logic;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import java.util.List;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHooks;

import shn.product.model.ShnProductRec;

public
class ShnProductHooks
	implements ObjectHooks <ShnProductRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	void beforeInsert (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductRec product) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"beforeInsert");

		) {

			product

				.setCreateTime (
					transaction.now ())

				.setUpdateTime (
					transaction.now ())

				.setShopifyNeedsSync (
					true)

				.setShopifySubCategoryCollectNeedsSync (
					true)

			;

		}

	}

	@Override
	public
	void beforeUpdate (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductRec product) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"beforeUpdate");

		) {

			product

				.setUpdateTime (
					transaction.now ())

				.setShopifyNeedsSync (
					true)

			;

		}

	}

	@Override
	public
	List <String> verifyData (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductRec object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"verifyData");

		) {

			if (
				object.getDeleted ()
				|| ! object.getActive ()
			) {
				return emptyList ();
			}

			ImmutableList.Builder <String> errorsBuilder =
				ImmutableList.builder ();

			if (

				isNull (
					object.getPublicTitle ())

				|| stringIsEmpty (
					object.getPublicTitle ())

			) {

				errorsBuilder.add (
					"Must set public title for active product");

			}

			if (

				isNull (
					object.getPublicDescription ())

				|| stringIsEmpty (
					object.getPublicDescription ().getText ())

			) {

				errorsBuilder.add (
					"Must set public description for active product");

			}

			if (

				isNull (
					object.getPublicContents ())

				|| stringIsEmpty (
					object.getPublicContents ().getText ())

			) {

				errorsBuilder.add (
					"Must set public contents for active product");

			}

			if (
				isNull (
					object.getSupplier ())
			) {

				errorsBuilder.add (
					"Must set supplier for active product");

			}

			return errorsBuilder.build ();

		}

	}

}
