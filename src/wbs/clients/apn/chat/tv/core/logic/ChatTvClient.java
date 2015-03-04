package wbs.clients.apn.chat.tv.core.logic;

import java.util.List;

import wbs.clients.apn.chat.tv.core.model.ChatTvMessageRec;
import wbs.clients.apn.chat.tv.core.model.ChatTvRec;
import wbs.platform.media.model.MediaRec;

public
interface ChatTvClient {

	void sendMessages (
		ChatTvRec chatTv,
		List<ChatTvMessageRec> messages,
		Mode mode);

	void uploadPicture (
		ChatTvRec chatTv,
		MediaRec media);

	enum Mode {
		feed,
		carousel
	}
}