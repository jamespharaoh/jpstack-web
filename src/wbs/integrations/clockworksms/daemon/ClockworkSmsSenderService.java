package wbs.integrations.clockworksms.daemon;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.sms.message.outbox.daemon.GenericSmsSenderService;

@SingletonComponent ("clockworkSmsSenderService")
public
class ClockworkSmsSenderService
	extends GenericSmsSenderService {

	// singleton dependencies

	@SingletonDependency
	ClockworkSmsSenderHelper clockworkSmsSenderHelper;

	// implementation

	@Override
	protected
	void setupService (
			@NonNull TaskLogger parentTaskLogger) {

		smsSenderHelper (
			clockworkSmsSenderHelper);

	}

}
