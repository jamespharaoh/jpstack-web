package wbs.apn.chat.scheme.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectHooks;

import wbs.apn.chat.scheme.model.ChatSchemeChargesRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;

public
class ChatSchemeChargesHooks
	implements ObjectHooks<ChatSchemeChargesRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	void createSingletons (
			@NonNull Transaction parentTransaction,
			@NonNull ObjectHelper <ChatSchemeChargesRec> chatSchemeChargesHelper,
			@NonNull ObjectHelper <?> chatSchemeHelper,
			@NonNull Record <?> parent) {

		if (! (parent instanceof ChatSchemeRec))
			return;

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createSingletons");

		) {

			ChatSchemeRec chatScheme =
				(ChatSchemeRec)
				parent;

			ChatSchemeChargesRec chatSchemeCharges =
				chatSchemeChargesHelper.insert (
					transaction,
					chatSchemeChargesHelper.createInstance ()

				.setChatScheme (
					chatScheme)

			);

			chatScheme

				.setCharges (
					chatSchemeCharges);

		}

	}

}