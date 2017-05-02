package wbs.smsapps.manualresponder.logic;

import java.util.List;

import com.google.common.base.Optional;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.database.Transaction;

import wbs.platform.affiliate.model.AffiliateRec;

import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateRec;

public
interface ManualResponderLogic {

	Pair <List <String>, Long> splitMessage (
			Transaction parentTransaction,
			ManualResponderTemplateRec template,
			Long maxLengthPerMultipartMessage,
			String messageString);

	void sendTemplateAutomatically (
			Transaction parentTransaction,
			ManualResponderRequestRec request,
			ManualResponderTemplateRec template);

	Optional <AffiliateRec> customerAffiliate (
			Transaction parentTransaction,
			ManualResponderNumberRec number);

}
