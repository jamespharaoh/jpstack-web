package wbs.integrations.oxygen8.model;

import java.io.Serializable;

import org.joda.time.Instant;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors (chain = true)
@Data
@EqualsAndHashCode
@ToString
public
class Oxygen8InboundLogSearch
	implements Serializable {

	Long routeId;

	Instant timestampAfter;
	Instant timestampBefore;

	String details;

}
