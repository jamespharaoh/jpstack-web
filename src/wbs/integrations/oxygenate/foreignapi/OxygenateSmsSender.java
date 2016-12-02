package wbs.integrations.oxygenate.foreignapi;

import javax.inject.Provider;

import wbs.framework.apiclient.GenericHttpSender;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;

@PrototypeComponent ("oxygenateSmsSender")
public
class OxygenateSmsSender
	extends GenericHttpSender <
		OxygenateSmsSender,
		OxygenateSmsSendRequest,
		OxygenateSmsSendResponse,
		OxygenateSmsSendHelper
	> {

	// prototype dependencies

	@PrototypeDependency
	Provider <OxygenateSmsSendHelper>
		oxygenateSmsSendHelperProvider;

	// life cycle

	@NormalLifecycleSetup
	public
	void init () {

		helper (
			oxygenateSmsSendHelperProvider.get ());

	}

}
