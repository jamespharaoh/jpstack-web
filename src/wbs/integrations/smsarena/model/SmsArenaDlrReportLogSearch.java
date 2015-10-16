package wbs.integrations.smsarena.model;

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
class SmsArenaDlrReportLogSearch {

	Instant timestampAfter;
	Instant timestampBefore;

	Integer routeId;

}
