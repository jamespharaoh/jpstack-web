package wbs.clients.apn.chat.scheme.model;

import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.object.ObjectHelper;
import wbs.framework.record.Record;

public
class ChatSchemeChargesHooks
	extends AbstractObjectHooks<ChatSchemeRec> {

	@Override
	public
	void createSingletons (
			ObjectHelper<ChatSchemeRec> chatSchemeHelper,
			ObjectHelper<?> parentHelper,
			Record<?> parent) {

		if (! ((Object) parent instanceof ChatSchemeRec))
			return;

		ChatSchemeRec chatScheme =
			(ChatSchemeRec)
			(Object)
			parent;

		ChatSchemeChargesRec chatSchemeCharges =
			chatSchemeHelper.insert (
				new ChatSchemeChargesRec ()
					.setChatScheme (chatScheme));

		chatScheme
			.setCharges (chatSchemeCharges);

	}

}