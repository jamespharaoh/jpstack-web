package wbs.smsapps.subscription.model;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

@Accessors (fluent = true)
@Data
public
class SubscriptionNumberSearch {

	Integer subscriptionId;

	String numberLike;

	Boolean active;

	Instant joinedAfter;
	Instant joinedBefore;

}
