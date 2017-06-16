package wbs.smsapps.manualresponder.model;

import java.util.Set;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.utils.time.TextualInterval;

@Accessors (fluent = true)
@Data
public
class ManualResponderReplySearch {

	Set <Long> manualResponderIds;

	Set <Long> userIds;

	TextualInterval timestamp;

}
