package wbs.integrations.oxygenate.daemon;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

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
	void setupService (
			@NonNull TaskLogger parentTaskLogger) {

		smsSenderHelper (
			oxygenateSmsSenderHelper);

	}

}
