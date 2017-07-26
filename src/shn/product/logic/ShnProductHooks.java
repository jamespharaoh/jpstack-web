package shn.product.logic;

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

}
