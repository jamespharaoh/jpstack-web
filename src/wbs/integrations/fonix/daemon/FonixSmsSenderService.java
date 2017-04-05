package wbs.integrations.fonix.daemon;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

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
	void setupService (
			@NonNull TaskLogger parentTaskLogger) {

		smsSenderHelper (
			fonixSmsSenderHelper);

	}

}
