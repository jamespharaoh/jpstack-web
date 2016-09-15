package wbs.apn.chat.graphs.console;

import wbs.framework.component.annotations.PrototypeComponent;

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
