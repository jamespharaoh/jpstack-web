package wbs.integrations.fonix.foreignapi;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class FonixMessageSendRequest {

	String url;
	String apiKey;
	String originator;
	List <String> numbers;
	String body;
	Boolean dummy = false;

}
