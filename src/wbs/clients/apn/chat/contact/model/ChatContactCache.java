package wbs.clients.apn.chat.contact.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

class ChatContactCache {

	Map<Pair<Integer,Integer>,ChatContactRec> byUserIds =
		new HashMap<Pair<Integer,Integer>,ChatContactRec> ();

}