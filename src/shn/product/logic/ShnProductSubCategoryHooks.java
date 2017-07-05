package shn.product.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHooks;

import shn.product.model.ShnProductCategoryRec;
import shn.product.model.ShnProductSubCategoryRec;

public
class ShnProductSubCategoryHooks
	implements ObjectHooks <ShnProductSubCategoryRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	void beforeInsert (
			@NonNull Transaction parentTransaction,
			@NonNull ShnProductSubCategoryRec subCategory) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"beforeInsert");

		) {

			ShnProductCategoryRec category =
				subCategory.getCategory ();

			subCategory

				.setDatabase (
					category.getDatabase ())

			;

		}

	}

}
