package wbs.clients.apn.chat.contact.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

class ChatContactCache {

	Map <Pair <Long,Long>, ChatContactRec> byUserIds =
		new HashMap<> ();

}