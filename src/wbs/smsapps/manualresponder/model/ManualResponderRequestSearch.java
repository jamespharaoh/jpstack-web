package wbs.smsapps.manualresponder.model;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.utils.TextualInterval;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode
@ToString
public
class ManualResponderRequestSearch
	implements Serializable {

	Long manualResponderId;
	Long manualResponderSliceId;

	String numberLike;

	TextualInterval createdTime;
	TextualInterval processedTime;

	Long processedByUserId;
	Long processedByUserSliceId;

	boolean filter;

	Collection<Long> filterManualResponderIds;
	Collection<Long> filterProcessedByUserIds;

	Order order =
		Order.timestampDesc;

	public static
	enum Order {
		timestampDesc
	}

}
