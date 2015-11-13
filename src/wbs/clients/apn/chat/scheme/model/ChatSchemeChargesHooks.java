package wbs.clients.apn.chat.scheme.model;

import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.object.ObjectHelper;
import wbs.framework.record.Record;

public
class ChatSchemeChargesHooks
	extends AbstractObjectHooks<ChatSchemeChargesRec> {

	@Override
	public
	void createSingletons (
			ObjectHelper<ChatSchemeChargesRec> chatSchemeChargesHelper,
			ObjectHelper<?> chatSchemeHelper,
			Record<?> parent) {

		if (! ((Object) parent instanceof ChatSchemeRec))
			return;

		ChatSchemeRec chatScheme =
			(ChatSchemeRec)
			(Object)
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