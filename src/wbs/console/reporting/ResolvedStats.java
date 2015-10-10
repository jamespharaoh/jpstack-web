package wbs.console.reporting;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.Instant;

@Accessors (fluent = true)
@Data
public
class ResolvedStats {

	Map<Object,Object> totals =
		new HashMap<Object,Object> ();

	Map<Pair<Object,Instant>,Object> steps =
		new HashMap<Pair<Object,Instant>,Object> ();

}
