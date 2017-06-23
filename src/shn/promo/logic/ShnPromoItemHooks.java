package shn.promo.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHooks;

import shn.promo.model.ShnPromoItemRec;
import shn.promo.model.ShnPromoRec;

public
class ShnPromoItemHooks
	implements ObjectHooks <ShnPromoItemRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	void beforeInsert (
			@NonNull Transaction parentTransaction,
			@NonNull ShnPromoItemRec promoItem) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"beforeInsert");

		) {

			ShnPromoRec promo =
				promoItem.getPromo ();

			promoItem

				.setIndex (
					promo.getNumItemsNotDeleted ())

			;

			for (
				ShnPromoItemRec otherItem
					: promo.getItems ()
			) {

				if (! otherItem.getDeleted ()) {
					continue;
				}

				otherItem

					.setIndex (
						otherItem.getIndex () + 1)

				;

			}

			promo

				.setNumItems (
					promo.getNumItems () + 1)

				.setNumItemsNotDeleted (
					promo.getNumItemsNotDeleted () + 1)

			;

		}

	}

}
