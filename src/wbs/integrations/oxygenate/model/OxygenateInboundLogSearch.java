package wbs.integrations.oxygenate.model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.utils.time.TextualInterval;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode
@ToString
public
class OxygenateInboundLogSearch
	implements Serializable {

	Long routeId;

	TextualInterval timestamp;

	String details;

	OxygenateInboundLogType type;

	Boolean success;

}
