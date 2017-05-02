package wbs.smsapps.subscription.model;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

@Accessors (fluent = true)
@Data
public
class SubscriptionNumberSearch
	implements Serializable {

	Long subscriptionId;

	String numberLike;

	Boolean active;

	Instant joinedAfter;
	Instant joinedBefore;

}
