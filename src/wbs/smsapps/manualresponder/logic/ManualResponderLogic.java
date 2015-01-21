package wbs.smsapps.manualresponder.logic;

import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateRec;

public
interface ManualResponderLogic {

	int maximumMessageLength (
			ManualResponderRequestRec request,
			ManualResponderTemplateRec template);

	int minimumMessageLength (
			ManualResponderRequestRec request,
			ManualResponderTemplateRec template);

}
