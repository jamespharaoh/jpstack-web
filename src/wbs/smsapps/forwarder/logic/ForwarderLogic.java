package wbs.smsapps.forwarder.logic;

import java.util.Collection;

import wbs.platform.media.model.MediaRec;
import wbs.smsapps.forwarder.logic.ForwarderLogicImplementation.SendTemplate;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec;
import wbs.smsapps.forwarder.model.ForwarderMessageOutRec;
import wbs.smsapps.forwarder.model.ForwarderRec;

public interface ForwarderLogic {

	/**
	 * Takes a SendTemplate and checks it for validity, filling in missing bits
	 * as it goes.
	 */
	boolean sendTemplateCheck (
			SendTemplate template);

	void sendTemplateSend (
			SendTemplate template);

	ForwarderMessageOutRec sendMessage (
			ForwarderRec
			forwarder,
			ForwarderMessageInRec fmIn,
			String message,
			String url,
			String numFrom,
			String numTo,
			String routeCode,
			String myId,
			Integer pri,
			Collection<MediaRec> medias);

}