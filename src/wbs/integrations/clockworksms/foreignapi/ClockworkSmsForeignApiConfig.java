package wbs.integrations.clockworksms.foreignapi;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.data.tools.DataFromXml;

@SingletonComponent ("clockworkSmsForeignApiConfig")
public
class ClockworkSmsForeignApiConfig {

	@SingletonComponent ("clockworkSmsForeignApiDataFromXml")
	public
	DataFromXml clockworkSmsForeignApiDataFromXml () {

		return new DataFromXml ()

			.registerBuilderClasses (
				ClockworkSmsMessageResponse.class,
				ClockworkSmsMessageResponse.SmsResp.class);

	}

}
