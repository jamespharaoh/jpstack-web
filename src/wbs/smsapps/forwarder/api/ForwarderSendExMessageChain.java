package wbs.smsapps.forwarder.api;

import java.util.ArrayList;
import java.util.List;

import wbs.smsapps.forwarder.logic.ForwarderLogicImplementation;

public
class ForwarderSendExMessageChain {

	Long replyToServerId;

	List <ForwarderSendExMessage> messages =
		new ArrayList<> ();

	ForwarderLogicImplementation.SendTemplate sendTemplate;

	List <String> errors =
		new ArrayList<> ();

	boolean ok =
		false;

}
