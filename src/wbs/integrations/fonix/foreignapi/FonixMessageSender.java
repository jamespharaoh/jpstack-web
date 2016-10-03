package wbs.integrations.fonix.foreignapi;

import javax.annotation.PostConstruct;
import javax.inject.Provider;

import wbs.framework.apiclient.GenericHttpSender;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;

@PrototypeComponent ("fonixMessageSender")
public
class FonixMessageSender
	extends GenericHttpSender <
		FonixMessageSender,
		FonixMessageSendRequest,
		FonixMessageSendResponse,
		FonixMessageSenderHelper
	> {

	// prototype dependencies

	@PrototypeDependency
	Provider <FonixMessageSenderHelper> fonixMessageSenderHelperProvider;

	// life cycle

	@PostConstruct
	public
	void init () {

		helper (
			fonixMessageSenderHelperProvider.get ());

	}

}
