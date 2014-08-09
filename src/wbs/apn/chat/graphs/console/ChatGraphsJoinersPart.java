package wbs.apn.chat.graphs.console;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("chatGraphsJoinersPart")
public
class ChatGraphsJoinersPart
	extends AbstractMonthlyGraphPart {

	{

		myLocalPart (
			"/chat.graphs.joiners");

		imageLocalPart (
			"/chat.graphs.joinersImage");

	}

}
