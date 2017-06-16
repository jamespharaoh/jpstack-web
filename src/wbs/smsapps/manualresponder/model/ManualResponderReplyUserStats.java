package wbs.smsapps.manualresponder.model;

import lombok.Data;

import wbs.platform.user.model.UserRec;

@Data
public
class ManualResponderReplyUserStats {

	ManualResponderRec manualResponder;
	UserRec user;

	Long numReplies;
	Long numCharacters;

}
