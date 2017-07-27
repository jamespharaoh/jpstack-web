package shn.product.logic;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.IterableUtils.iterableFilter;
import static wbs.utils.collection.IterableUtils.iterableIsNotEmpty;
import static wbs.utils.collection.IterableUtils.iterableOnlyItemRequired;
import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.Misc.stringTrim;
import static wbs.utils.etc.Misc.todo;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.parseInteger;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.dynamicCastRequired;
import static wbs.utils.string.StringUtils.stringDoesNotStartWith;
import static wbs.utils.string.StringUtils.stringExtractRequired;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHooks;

import wbs.utils.data.Pair;

import shn.core.model.ShnDatabaseRec;
import shn.product.model.ShnProductRec;
import shn.product.model.ShnProductVariantRec;
import shn.product.model.ShnProductVariantTypeObjectHelper;
import shn.product.model.ShnProductVariantTypeRec;
import shn.product.model.ShnProductVariantValueRec;

public
class ShnProductVariantHooks
	implements ObjectHooks <ShnProductVariantRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	ShnProductVariantTypeObjectHelper variantTypeHelper;

	// public implementation

	@Override
	public
	void beforeInsert (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductVariantRec productVariant) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"beforeInsert");

		) {

			ShnProductRec product =
				productVariant.getProduct ();

			if (
				stringDoesNotStartWith (
					product.getItemNumber (),
					productVariant.getItemNumber ())
			) {
				throw todo ();
			}

			productVariant

				.setDatabase (
					product.getDatabase ())

			;

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
	void beforeUpdate (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductVariantRec productVariant) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"beforeUpdate");

		) {

			ShnProductRec product =
				productVariant.getProduct ();

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
	Object getDynamic (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductVariantRec productVariant,
			@NonNull String name) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getDynamic");

		) {

			ShnProductRec product =
				productVariant.getProduct ();

			ShnDatabaseRec shnDatabase =
				product.getDatabase ();

			String variantIndexString =
				stringExtractRequired (
					"variant.",
					"",
					name);

			Optional <Long> variantIndexOptional =
				parseInteger (
					variantIndexString);

			if (
				optionalIsPresent (
					variantIndexOptional)
			) {
				return null;
			}

			ShnProductVariantTypeRec variantType =
				variantTypeHelper.findByCodeRequired (
					transaction,
					shnDatabase,
					variantIndexString);

			return iterableOnlyItemRequired (
				iterableFilter (
					productVariant.getVariantValues (),
					variantValue ->
						referenceEqualWithClass (
							ShnProductVariantTypeRec.class,
							variantType,
							variantValue.getType ())));

		}

	}

	@Override
	public
	Optional <String> setDynamic (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductVariantRec productVariant,
			@NonNull String name,
			@NonNull Optional <?> newValueOptional) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getDynamic");

		) {

			ShnProductRec product =
				productVariant.getProduct ();

			ShnDatabaseRec shnDatabase =
				product.getDatabase ();

			String variantIndexString =
				stringExtractRequired (
					"variant.",
					"",
					name);

			Optional <Long> variantIndexOptional =
				parseInteger (
					variantIndexString);

			// remove old value

			Optional <ShnProductVariantValueRec> oldValueOptional;

			if (
				optionalIsNotPresent (
					variantIndexOptional)
			) {

				ShnProductVariantTypeRec variantType =
					variantTypeHelper.findByCodeRequired (
						transaction,
						shnDatabase,
						variantIndexString);

				ShnProductVariantValueRec oldValue =
					iterableOnlyItemRequired (
						iterableFilter (
							productVariant.getVariantValues (),
							variantValue ->
								referenceEqualWithClass (
									ShnProductVariantTypeRec.class,
									variantType,
									variantValue.getType ())));

				productVariant.getVariantValues ().remove (
					oldValue);

				oldValueOptional =
					optionalOf (
						oldValue);

			} else {

				oldValueOptional =
					optionalAbsent ();

			}

			// set new value

			if (
				optionalIsPresent (
					newValueOptional)
			) {

				ShnProductVariantValueRec newValue =
					dynamicCastRequired (
						ShnProductVariantValueRec.class,
						optionalGetRequired (
							newValueOptional));

				if (
					optionalIsPresent (
						oldValueOptional)
				) {

					ShnProductVariantValueRec oldValue =
						optionalGetRequired (
							oldValueOptional);

					if (
						referenceNotEqualWithClass (
							ShnProductVariantTypeRec.class,
							oldValue.getType (),
							newValue.getType ())
					) {
						throw todo ();
					}

				}

				if (
					iterableIsNotEmpty (
						iterableFilter (
							productVariant.getVariantValues (),
							variantValue ->
								referenceEqualWithClass (
									ShnProductVariantTypeRec.class,
									newValue.getType (),
									variantValue.getType ())))
				) {
					throw todo ();
				}

				productVariant.getVariantValues ().add (
					newValue);

				return optionalOf (
					stringFormat (
						"variant.%s",
						newValue.getType ().getCode ()));

			} else {

				ShnProductVariantValueRec oldValue =
					optionalGetRequired (
						oldValueOptional);

				return optionalOf (
					stringFormat (
						"variant.%s",
						oldValue.getType ().getCode ()));

			}

		}

	}

	@Override
	public
	List <Pair <Record <?>, String>> verifyData (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductVariantRec variant,
			@NonNull Boolean recurse) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"verifyData");

		) {

			if (
				variant.getDeleted ()
				|| ! variant.getActive ()
			) {
				return emptyList ();
			}

			ImmutableList.Builder <Pair <Record <?>, String>> errorsBuilder =
				ImmutableList.builder ();

			if (

				isNull (
					variant.getPublicTitle ())

				|| stringIsEmpty (
					stringTrim (
						variant.getPublicTitle ()))

			) {

				errorsBuilder.add (
					Pair.of (
						variant,
						"Must set public title for active product variant"));

			}

			if (
				isNull (
					variant.getSupplierReference ())
			) {

				errorsBuilder.add (
					Pair.of (
						variant,
						stringFormat (
							"Must set supplier reference for active product ",
							"variant")));

			}

			if (
				isNull (
					variant.getImage ())
			) {

				errorsBuilder.add (
					Pair.of (
						variant,
						stringFormat (
							"Must set image for active product variant")));

			}

			if (
				isNull (
					variant.getRecommendedRetailPrice ())
			) {

				errorsBuilder.add (
					Pair.of (
						variant,
						stringFormat (
							"Must set recommended retail price for active ",
							"product variant")));

			}

			if (
				isNull (
					variant.getShoppingNationPrice ())
			) {

				errorsBuilder.add (
					Pair.of (
						variant,
						stringFormat (
							"Must set Shopping Nation price for active ",
							"product variant")));

			}

			if (
				isNull (
					variant.getCostPrice ())
			) {

				errorsBuilder.add (
					Pair.of (
						variant,
						stringFormat (
							"Must set cost price for active product variant")));

			}

			if (
				isNull (
					variant.getPostageAndPackaging ())
			) {

				errorsBuilder.add (
					Pair.of (
						variant,
						stringFormat (
							"Must set postage and packaging for active ",
							"product variant")));

			}

			if (
				isNull (
					variant.getStockQuantity ())
			) {

				errorsBuilder.add (
					Pair.of (
						variant,
						stringFormat (
							"Must set stock quantity for active product ",
							"variant")));

			}

			return errorsBuilder.build ();

		}

	}

}
