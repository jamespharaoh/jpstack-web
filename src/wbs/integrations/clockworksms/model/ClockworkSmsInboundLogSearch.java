package wbs.integrations.clockworksms.model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.utils.TextualInterval;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode
@ToString
public 
class ClockworkSmsInboundLogSearch
	implements Serializable {

	Long routeId;

	TextualInterval timestamp;

	String details;

	ClockworkSmsInboundLogType type;

	Boolean success;

}
