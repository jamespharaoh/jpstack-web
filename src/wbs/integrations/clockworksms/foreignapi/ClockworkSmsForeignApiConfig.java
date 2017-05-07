package wbs.integrations.clockworksms.foreignapi;

import javax.inject.Provider;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;

@SingletonComponent ("clockworkSmsForeignApiConfig")
public
class ClockworkSmsForeignApiConfig {

	// prototype dependencies

	@PrototypeDependency
	Provider <DataFromXmlBuilder> dataFromXmlBuilderProvider;

	// components

	@SingletonComponent ("clockworkSmsForeignApiDataFromXml")
	public
	DataFromXml clockworkSmsForeignApiDataFromXml () {

		return dataFromXmlBuilderProvider.get ()

			.registerBuilderClasses (
				ClockworkSmsMessageResponse.class,
				ClockworkSmsMessageResponse.SmsResp.class)

			.build ();

	}

}
