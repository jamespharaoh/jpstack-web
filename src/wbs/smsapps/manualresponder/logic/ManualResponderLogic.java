package wbs.smsapps.manualresponder.logic;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateRec;

public
interface ManualResponderLogic {

	Pair<List<String>,Long> splitMessage (
			ManualResponderTemplateRec template,
			Long maxLengthPerMultipartMessage,
			String messageString);

	void sendTemplateAutomatically (
			ManualResponderRequestRec request,
			ManualResponderTemplateRec template);

}
