package wbs.apn.chat.infosite.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jadira.usertype.dateandtime.joda.PersistentInstantAsTimestamp;
import org.joda.time.Instant;

import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.LinkField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatInfoSiteRec
	implements CommonRecord<ChatInfoSiteRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatUserRec chatUser;

	// TODO index?

	// details

	@SimpleField (
		hibernateTypeHelper = PersistentInstantAsTimestamp.class)
	Instant createTime;

	@SimpleField (
		hibernateTypeHelper = PersistentInstantAsTimestamp.class)
	Instant expireTime;

	@SimpleField
	String token;

	// statistics

	@SimpleField (
		hibernateTypeHelper = PersistentInstantAsTimestamp.class)
	Instant firstViewTime;

	@SimpleField (
		hibernateTypeHelper = PersistentInstantAsTimestamp.class)
	Instant lastViewTime;

	@SimpleField (
		hibernateTypeHelper = PersistentInstantAsTimestamp.class)
	Instant lastExpiredTime;

	@SimpleField
	Integer numViews;

	@SimpleField
	Integer numExpired;

	// children

	@LinkField (
		table = "chat_info_site_user",
		index = "index")
	List<ChatUserRec> otherChatUsers =
		new ArrayList<ChatUserRec> ();

	@Override
	public
	int compareTo (
			Record<ChatInfoSiteRec> otherRecord) {

		ChatInfoSiteRec other =
			(ChatInfoSiteRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getCreateTime (),
				getCreateTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}