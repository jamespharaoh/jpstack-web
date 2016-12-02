package wbs.integrations.oxygenate.daemon;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

import wbs.sms.message.outbox.daemon.GenericSmsSenderService;

@SingletonComponent ("oxygenateSmsSenderService")
public
class OxygenateSmsSenderService
	extends GenericSmsSenderService {

	// singleton dependencies

	@SingletonDependency
	OxygenateSmsSenderServiceHelper oxygenateSmsSenderHelper;

	// implementation

	@Override
	protected
	void init () {

		smsSenderHelper (
			oxygenateSmsSenderHelper);

	}

}
