package wbs.smsapps.autoresponder.console;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.utils.time.TextualInterval;

@Accessors (fluent = true)
@Data
public
class AutoResponderVotesForm {

	TextualInterval timePeriod;

}
