package wbs.smsapps.forwarder.api;

import java.util.Collection;
import java.util.Set;

import wbs.platform.media.model.MediaRec;

import wbs.smsapps.forwarder.logic.ForwarderLogicImplementation;

public
class ForwarderSendExMessage {

	ForwarderMessageType type;

	String numTo;
	String numFrom;
	String message;
	String url;
	String subject;

	String clientId;
	String route;
	String service;

	Long pri;
	Long retryDays;

	Set <String> tags;

	ForwarderLogicImplementation.SendPart part;

	Collection <MediaRec> medias = null;

}
