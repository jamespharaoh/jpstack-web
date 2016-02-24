package wbs.smsapps.manualresponder.model;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.Interval;

@Accessors (fluent = true)
@Data
public
class ManualResponderNumberSearch
	implements Serializable {

	Integer manualResponderId;

	String number;

	Interval firstRequest;
	Interval lastRequest;

	String notes;

}
