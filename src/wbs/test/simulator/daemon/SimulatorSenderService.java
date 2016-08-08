package wbs.test.simulator.daemon;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.message.outbox.daemon.GenericSmsSenderService;

@SingletonComponent ("simulatorSenderService")
public
class SimulatorSenderService
	extends GenericSmsSenderService {

	// dependencies

	@Inject
	SimulatorSenderHelper simulatorSenderHelper;

	// implementation

	@Override
	protected
	void init () {

		smsSenderHelper (
			simulatorSenderHelper);

	}

}
