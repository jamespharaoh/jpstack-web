package wbs.smsapps.forwarder.logic;

import java.util.Collection;

import wbs.framework.database.Transaction;

import wbs.platform.media.model.MediaRec;

import wbs.smsapps.forwarder.logic.ForwarderLogicImplementation.SendTemplate;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec;
import wbs.smsapps.forwarder.model.ForwarderMessageOutRec;
import wbs.smsapps.forwarder.model.ForwarderRec;

public
interface ForwarderLogic {

	boolean sendTemplateCheck (
			Transaction parentTransaction,
			SendTemplate template);

	void sendTemplateSend (
			Transaction parentTransaction,
			SendTemplate template);

	ForwarderMessageOutRec sendMessage (
			Transaction parentTransaction,
			ForwarderRec forwarder,
			ForwarderMessageInRec fmIn,
			String message,
			String url,
			String numFrom,
			String numTo,
			String routeCode,
			String myId,
			Long pri,
			Collection <MediaRec> medias);

}