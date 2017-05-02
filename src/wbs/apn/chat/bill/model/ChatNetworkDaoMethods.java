package wbs.apn.chat.bill.model;

import wbs.framework.database.Transaction;

import wbs.sms.network.model.NetworkRec;

import wbs.apn.chat.core.model.ChatRec;

public
interface ChatNetworkDaoMethods {

	ChatNetworkRec find (
			Transaction parentTransaction,
			ChatRec chat,
			NetworkRec network);

}