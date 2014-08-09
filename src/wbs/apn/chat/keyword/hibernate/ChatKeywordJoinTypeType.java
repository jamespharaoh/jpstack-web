package wbs.apn.chat.keyword.hibernate;

import java.sql.Types;

import wbs.apn.chat.keyword.model.ChatKeywordJoinType;
import wbs.framework.hibernate.EnumUserType;

public
class ChatKeywordJoinTypeType
	extends EnumUserType<String,ChatKeywordJoinType> {

	{

		sqlType (Types.VARCHAR);
		enumClass (ChatKeywordJoinType.class);

		add ("chat_simple", ChatKeywordJoinType.chatSimple);
		add ("chat_set_info", ChatKeywordJoinType.chatSetInfo);
		add ("chat_next", ChatKeywordJoinType.chatNext);
		add ("chat_location", ChatKeywordJoinType.chatLocation);
		add ("chat_dob", ChatKeywordJoinType.chatDob);
		add ("chat_pics", ChatKeywordJoinType.chatPics);
		add ("chat_videos", ChatKeywordJoinType.chatVideos);
		add ("date_simple", ChatKeywordJoinType.dateSimple);
		add ("date_set_info", ChatKeywordJoinType.dateSetInfo);
		add ("date_dob", ChatKeywordJoinType.dateDob);

	}
}
