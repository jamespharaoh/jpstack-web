package wbs.clients.apn.chat.scheme.model;

import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectHooks;
import wbs.framework.record.Record;

public
class ChatSchemeChargesHooks
	implements ObjectHooks<ChatSchemeChargesRec> {

	@Override
	public
	void createSingletons (
			ObjectHelper<ChatSchemeChargesRec> chatSchemeChargesHelper,
			ObjectHelper<?> chatSchemeHelper,
			Record<?> parent) {

		if (! (parent instanceof ChatSchemeRec))
			return;

		ChatSchemeRec chatScheme =
			(ChatSchemeRec)
			parent;

		ChatSchemeChargesRec chatSchemeCharges =
			chatSchemeChargesHelper.insert (
				chatSchemeChargesHelper.createInstance ()

			.setChatScheme (
				chatScheme)

		);

		chatScheme

			.setCharges (
				chatSchemeCharges);

	}

}