package wbs.integrations.fonix.daemon;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.sms.message.outbox.daemon.GenericSmsSenderService;

@SingletonComponent ("fonixSmsSenderService")
public
class FonixSmsSenderService
	extends GenericSmsSenderService {

	// singleton dependencies

	@SingletonDependency
	FonixSmsSenderHelper fonixSmsSenderHelper;

	// implementation

	@Override
	protected
	void init () {

		smsSenderHelper (
			fonixSmsSenderHelper);

	}

}
