package wbs.smsapps.manualresponder.model;

import java.util.Collection;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.utils.time.TextualInterval;

@Accessors (fluent = true)
@Data
public
class ManualResponderReplyStatsSearch {

	Collection <Long> manualResponderIds;
	Collection <Long> userIds;

	TextualInterval timestamp;

	Collection <Long> filterManualResponderIds;
	Collection <Long> filterUserIds;

}
