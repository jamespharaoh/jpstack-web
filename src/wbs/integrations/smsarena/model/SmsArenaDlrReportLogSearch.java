package wbs.integrations.smsarena.model;

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
class SmsArenaDlrReportLogSearch
	implements Serializable {

	Instant timestampAfter;
	Instant timestampBefore;

	Long routeId;

}
