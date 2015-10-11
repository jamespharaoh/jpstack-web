package wbs.clients.apn.chat.bill.model;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.sms.network.model.NetworkRec;

public
interface ChatNetworkDaoMethods {

	ChatNetworkRec find (
			ChatRec chat,
			NetworkRec network);

}