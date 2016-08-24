package wbs.clients.apn.chat.scheme.logic;

import wbs.clients.apn.chat.scheme.model.ChatSchemeChargesRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectHooks;

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