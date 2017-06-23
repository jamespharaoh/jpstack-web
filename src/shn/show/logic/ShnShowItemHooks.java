package shn.show.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHooks;

import shn.show.model.ShnShowItemRec;
import shn.show.model.ShnShowRec;

public
class ShnShowItemHooks
	implements ObjectHooks <ShnShowItemRec> {

	// singleton dependencies

	/*
	@SingletonDependency
	AffiliateTypeDao affiliateTypeDao;

	@SingletonDependency
	Database database;
	*/

	@ClassSingletonDependency
	LogContext logContext;

	/*
	@SingletonDependency
	ObjectTypeDao objectTypeDao;
	*/

	// public implementation

	@Override
	public
	void beforeInsert (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShowItemRec showItem) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"beforeInsert");

		) {

			ShnShowRec show =
				showItem.getShow ();

			showItem

				.setIndex (
					show.getNumItemsNotDeleted ())

			;

			for (
				ShnShowItemRec otherItem
					: show.getItems ()
			) {

				if (! otherItem.getDeleted ()) {
					continue;
				}

				otherItem

					.setIndex (
						otherItem.getIndex () + 1)

				;

			}

			show

				.setNumItems (
					show.getNumItems () + 1)

				.setNumItemsNotDeleted (
					show.getNumItemsNotDeleted () + 1)

			;

		}

	}

}
