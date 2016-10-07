package wbs.integrations.oxygen8.model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.integrations.fonix.model.FonixInboundLogType;
import wbs.utils.time.TextualInterval;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode
@ToString
public
class Oxygen8InboundLogSearch
	implements Serializable {

	Long routeId;

	TextualInterval timestamp;

	String details;

	FonixInboundLogType type;

	Boolean success;

}
