package wbs.test.simulator.daemon;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.sms.message.outbox.daemon.GenericSmsSenderService;

@SingletonComponent ("simulatorSenderService")
public
class SimulatorSenderService
	extends GenericSmsSenderService {

	// singleton dependencies

	@SingletonDependency
	SimulatorSenderHelper simulatorSenderHelper;

	// implementation

	@Override
	protected
	void init () {

		smsSenderHelper (
			simulatorSenderHelper);

	}

}
