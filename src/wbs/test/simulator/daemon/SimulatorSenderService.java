package wbs.test.simulator.daemon;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

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
	void setupService (
			@NonNull TaskLogger parentTaskLogger) {

		smsSenderHelper (
			simulatorSenderHelper);

	}

}
