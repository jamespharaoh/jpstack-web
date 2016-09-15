package wbs.smsapps.manualresponder.model;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.utils.time.TextualInterval;

@Accessors (fluent = true)
@Data
public
class ManualResponderNumberSearch
	implements Serializable {

	Long manualResponderId;

	String number;

	TextualInterval firstRequest;
	TextualInterval lastRequest;

	String notes;

}
