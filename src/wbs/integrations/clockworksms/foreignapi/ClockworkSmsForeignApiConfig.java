package wbs.integrations.clockworksms.foreignapi;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;

@SingletonComponent ("clockworkSmsForeignApiConfig")
public
class ClockworkSmsForeignApiConfig {

	@SingletonComponent ("clockworkSmsForeignApiDataFromXml")
	public
	DataFromXml clockworkSmsForeignApiDataFromXml () {

		return new DataFromXmlBuilder ()

			.registerBuilderClasses (
				ClockworkSmsMessageResponse.class,
				ClockworkSmsMessageResponse.SmsResp.class)

			.build ();

	}

}
