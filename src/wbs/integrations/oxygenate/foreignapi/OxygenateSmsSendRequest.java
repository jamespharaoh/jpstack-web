package wbs.integrations.oxygenate.foreignapi;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class OxygenateSmsSendRequest {

	String relayUrl;

	String reference;
	String campaignId;
	String username;
	String password;
	String multipart;
	String shortcode;
	String mask;
	String channel;
	String msisdn;
	String content;
	String premium;

}
