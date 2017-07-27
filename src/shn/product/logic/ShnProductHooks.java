package shn.product.logic;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.IterableUtils.iterableFlatMap;
import static wbs.utils.etc.Misc.stringTrim;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import java.util.List;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHooks;
import wbs.framework.object.ObjectManager;

import wbs.utils.data.Pair;

import shn.product.model.ShnProductRec;

public
class ShnProductHooks
	implements ObjectHooks <ShnProductRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	ObjectManager objectManager;

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
	List <Pair <Record <?>, String>> verifyData (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductRec product,
			@NonNull Boolean recurse) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"verifyData");

		) {

			if (
				product.getDeleted ()
				|| ! product.getActive ()
			) {
				return emptyList ();
			}

			ImmutableList.Builder <Pair <Record <?>, String>> errorsBuilder =
				ImmutableList.builder ();

			if (

				isNull (
					product.getPublicTitle ())

				|| stringIsEmpty (
					stringTrim (
						product.getPublicTitle ()))

			) {

				errorsBuilder.add (
					Pair.of (
						product,
						"Must set public title for active product"));

			}

			if (

				isNull (
					product.getPublicDescription ())

				|| stringIsEmpty (
					stringTrim (
						product.getPublicDescription ().getText ()))

			) {

				errorsBuilder.add (
					Pair.of (
						product,
						"Must set public description for active product"));

			}

			if (

				isNull (
					product.getPublicContents ())

				|| stringIsEmpty (
					stringTrim (
						product.getPublicContents ().getText ()))

			) {

				errorsBuilder.add (
					Pair.of (
						product,
						"Must set public contents for active product"));

			}

			if (
				isNull (
					product.getSupplier ())
			) {

				errorsBuilder.add (
					Pair.of (
						product,
						"Must set supplier for active product"));

			}

			if (recurse) {

				errorsBuilder.addAll (
					iterableFlatMap (
						product.getVariants (),
						variant ->
							objectManager.verifyData (
								transaction,
								variant,
								true)));

			}

			// TODO recurse

			return errorsBuilder.build ();

		}

	}

}
