package wbs.integrations.clockworksms.daemon;

import javax.inject.Inject;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.sms.message.outbox.daemon.GenericSmsSenderService;

@SingletonComponent ("clockworkSmsSenderService")
public
class ClockworkSmsSenderService
	extends GenericSmsSenderService {

	// dependencies

	@Inject
	ClockworkSmsSenderHelper clockworkSmsSenderHelper;

	// implementation

	@Override
	protected
	void init () {

		smsSenderHelper (
			clockworkSmsSenderHelper);

	}

}
