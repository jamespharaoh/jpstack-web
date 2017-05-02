package wbs.apn.chat.contact.hibernate;

import java.sql.Types;

import wbs.framework.hibernate.EnumUserType;

import wbs.apn.chat.contact.model.ChatUserInitiationReason;

public
class ChatUserInitiationReasonType
	extends EnumUserType<String,ChatUserInitiationReason> {

	{

		sqlType (Types.VARCHAR);
		enumClass (ChatUserInitiationReason.class);

		add ("join", ChatUserInitiationReason.joinUser);
		add ("quiet", ChatUserInitiationReason.quietUser);
		add ("alarm", ChatUserInitiationReason.alarm);
		add ("alarm-set", ChatUserInitiationReason.alarmSet);
		add ("alarm-cancel", ChatUserInitiationReason.alarmCancel);
		add ("alarm-ignore", ChatUserInitiationReason.alarmIgnore);

	}

}
