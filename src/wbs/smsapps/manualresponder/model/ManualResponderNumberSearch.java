package wbs.smsapps.manualresponder.model;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.utils.TextualInterval;

@Accessors (fluent = true)
@Data
public
class ManualResponderNumberSearch
	implements Serializable {

	Integer manualResponderId;

	String number;

	TextualInterval firstRequest;
	TextualInterval lastRequest;

	String notes;

}
