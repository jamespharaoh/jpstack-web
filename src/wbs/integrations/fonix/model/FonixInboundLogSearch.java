package wbs.integrations.fonix.model;

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
class FonixInboundLogSearch
	implements Serializable {

	Long routeId;

	TextualInterval timestamp;

	String details;

	FonixInboundLogType type;

	Boolean success;

}
