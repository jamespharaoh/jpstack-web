package wbs.platform.event.model;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

@Accessors (fluent = true)
@Data
public
class EventSearch {

	Instant timestampAfter;
	Instant timestampBefore;

}
