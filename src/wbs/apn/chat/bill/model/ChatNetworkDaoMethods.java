package wbs.apn.chat.bill.model;

import wbs.apn.chat.bill.model.ChatNetworkRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.sms.network.model.NetworkRec;

public
interface ChatNetworkDaoMethods {

	ChatNetworkRec find (
			ChatRec chat,
			NetworkRec network);

}