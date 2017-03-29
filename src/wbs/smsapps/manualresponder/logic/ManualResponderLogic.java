package wbs.smsapps.manualresponder.logic;

import java.util.List;

import com.google.common.base.Optional;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.logging.TaskLogger;

import wbs.platform.affiliate.model.AffiliateRec;

import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateRec;

public
interface ManualResponderLogic {

	Pair <List <String>, Long> splitMessage (
			ManualResponderTemplateRec template,
			Long maxLengthPerMultipartMessage,
			String messageString);

	void sendTemplateAutomatically (
			TaskLogger parentTaskLogger,
			ManualResponderRequestRec request,
			ManualResponderTemplateRec template);

	Optional <AffiliateRec> customerAffiliate (
			ManualResponderNumberRec number);

}
