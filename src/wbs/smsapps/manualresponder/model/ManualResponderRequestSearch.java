package wbs.smsapps.manualresponder.model;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.joda.time.Interval;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode
@ToString
public
class ManualResponderRequestSearch
	implements Serializable {

	Integer manualResponderId;
	Integer manualResponderSliceId;

	String numberLike;

	Interval createdTime;
	Interval processedTime;

	Integer processedByUserId;
	Integer processedByUserSliceId;

	boolean filter;

	Collection<Integer> filterManualResponderIds;
	Collection<Integer> filterProcessedByUserIds;

	Order order =
		Order.timestampDesc;

	public static
	enum Order {
		timestampDesc
	}

}
