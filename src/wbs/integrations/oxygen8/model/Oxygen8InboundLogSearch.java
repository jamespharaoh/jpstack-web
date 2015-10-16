package wbs.integrations.oxygen8.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

@Accessors (chain = true)
@Data
@EqualsAndHashCode
@ToString
public
class Oxygen8InboundLogSearch {

	Integer routeId;

	Instant timestampAfter;
	Instant timestampBefore;

	String details;

}
