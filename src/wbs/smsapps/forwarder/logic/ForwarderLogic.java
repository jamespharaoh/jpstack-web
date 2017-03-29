package wbs.smsapps.forwarder.logic;

import java.util.Collection;

import wbs.framework.logging.TaskLogger;

import wbs.platform.media.model.MediaRec;

import wbs.smsapps.forwarder.logic.ForwarderLogicImplementation.SendTemplate;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec;
import wbs.smsapps.forwarder.model.ForwarderMessageOutRec;
import wbs.smsapps.forwarder.model.ForwarderRec;

public
interface ForwarderLogic {

	boolean sendTemplateCheck (
			TaskLogger parentTaskLogger,
			SendTemplate template);

	void sendTemplateSend (
			TaskLogger parentTaskLogger,
			SendTemplate template);

	ForwarderMessageOutRec sendMessage (
			TaskLogger parentTaskLogger,
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