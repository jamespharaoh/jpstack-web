package wbs.clients.apn.chat.contact.logic;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import wbs.clients.apn.chat.contact.model.ChatContactRec;

class ChatContactCache {

	Map <Pair <Long,Long>, ChatContactRec> byUserIds =
		new HashMap<> ();

}