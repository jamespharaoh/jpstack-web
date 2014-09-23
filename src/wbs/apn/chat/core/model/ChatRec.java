package wbs.apn.chat.core.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.apn.chat.ad.model.ChatAdTemplateRec;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.apn.chat.keyword.model.ChatKeywordRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.apn.chat.user.info.model.ChatProfileFieldRec;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.platform.currency.model.CurrencyRec;
import wbs.platform.scaffold.model.SliceRec;
import wbs.sms.gazetteer.model.GazetteerRec;
import wbs.sms.locator.model.LocatorRec;
import wbs.sms.number.format.model.NumberFormatRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public
class ChatRec
	implements MajorRecord<ChatRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SliceRec slice;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String description;

	@DeletedField
	Boolean deleted = false;

	// settings

	@SimpleField
	Integer timeLogoff = 0;

	@SimpleField
	Integer timeSend = 0;

	@SimpleField
	Integer timeReceive = 0;

	@SimpleField
	Integer timeInfo = 0;

	@SimpleField
	Integer timeWebLogoff = 0;

	@SimpleField
	Integer timeName = 0;

	@SimpleField
	Integer timeNameJoin = 0;

	@SimpleField
	Integer timePicHint = 0;

	@SimpleField
	Integer timeJoinOutboundMin = 0;

	@SimpleField
	Integer timeJoinOutboundMax = 0;

	@SimpleField
	Integer timeQuietOutbound = 0;

	@SimpleField
	Integer timeSignupTimeout = 0;

	@SimpleField
	Boolean autoAdultVerify = false;

	@SimpleField (nullable = true)
	Gender gender;

	@SimpleField (nullable = true)
	Orient orient;

	@SimpleField
	Boolean autoJoinChat = false;

	@SimpleField
	Boolean autoJoinDate = false;

	@SimpleField
	Boolean autoJoinOnSend = false;

	@ReferenceField (
		nullable = true)
	NumberFormatRec numberFormat;

	@ReferenceField (
		nullable = true)
	CurrencyRec currency;

	@ReferenceField (
		nullable = true)
	GazetteerRec gazetteer;

	@SimpleField
	String defaultBrandName = "Chat service";

	@SimpleField (
		nullable = true)
	String timezone;

	// other stuff

	@SimpleField (nullable = true)
	Date lastMonitorSwap;

	@SimpleField
	Integer timeMonitorSwap = 0;

	@ReferenceField (nullable = true)
	LocatorRec locator;

	@SimpleField (nullable = true)
	String jigsawApplicationIdentifier;

	@SimpleField
	Integer minMonitorMessageLength = 80;

	@ReferenceField (nullable = true)
	ChatUserRec systemChatUser;

	@SimpleField
	Integer maxAlarmTime = 0;

	// adult service ads

	@ReferenceField (nullable = true)
	ChatRec adultAdsChat;

	@SimpleField
	Integer adultAdsTime = 0;

	// switches

	@SimpleField
	Boolean datingEnabled = false;

	@SimpleField
	Boolean joinWarningEnabled = false;

	@SimpleField
	Boolean errorOnUnrecognised = false;

	// ad settings

	@SimpleField
	Integer adTimeFirst = 0;

	@SimpleField
	Integer adTime = 0;

	@SimpleField
	Integer adCount = 0;

	// queue stuff

	@SimpleField
	Integer chatQueuePreferredTime = 0;

	@SimpleField
	Integer messageQueuePreferredTime = 0;

	@SimpleField
	Integer helpQueuePreferredTime = 0;

	@SimpleField
	Integer infoQueuePreferredTime = 0;

	// related objects

	@CollectionField (
		index = "keyword",
		orderBy = "keyword")
	Map<String,ChatKeywordRec> keywords =
		new HashMap<String,ChatKeywordRec> ();

	@CollectionField (
		orderBy = "code")
	Set<ChatSchemeRec> chatSchemes =
		new TreeSet<ChatSchemeRec> ();

	@CollectionField (
		orderBy = "code")
	Set<ChatHelpTemplateRec> chatHelpTemplates =
		new TreeSet<ChatHelpTemplateRec> ();

	@CollectionField
	Set<ChatAdTemplateRec> chatAdTemplates =
		new TreeSet<ChatAdTemplateRec> ();

	@CollectionField (
		index = "code")
	Map<String,ChatProfileFieldRec> profileFields =
		new TreeMap<String,ChatProfileFieldRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatRec> otherRecord) {

		ChatRec other =
			(ChatRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSlice (),
				other.getSlice ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
