package wbs.apn.chat.scheme.model;

import java.util.Set;
import java.util.TreeSet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.core.model.ChatRec;
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
import wbs.framework.entity.annotations.SlaveField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.sms.keyword.model.KeywordSetRec;
import wbs.sms.magicnumber.model.MagicNumberSetRec;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.model.RouterRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class ChatSchemeRec
	implements MajorRecord<ChatSchemeRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatRec chat;

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

	@ReferenceField (
		nullable = true)
	RouteRec rbBillRoute;

	@ReferenceField (
		nullable = true)
	RouterRec rbFreeRouter;

	@SimpleField
	String rbNumber = "";

	@ReferenceField (
		nullable = true)
	MagicNumberSetRec magicNumberSet;

	@ReferenceField (
		nullable = true)
	RouterRec magicRouter;

	@ReferenceField (
		nullable = true)
	RouteRec mmsRoute;

	@ReferenceField (
		nullable = true)
	RouterRec mmsFreeRouter;

	@SimpleField
	String mmsNumber = "";

	@ReferenceField (
		nullable = true)
	KeywordSetRec mmsKeywordSet;

	@ReferenceField (
		nullable = true)
	RouterRec wapRouter;

	@SimpleField
	Integer initialCredit = 0;

	@SimpleField
	String style = "";

	@SimpleField
	Boolean approvals = true;

	@SimpleField (
		nullable = true)
	String timezone;

	// children

	@SlaveField
	ChatSchemeChargesRec charges;

	@CollectionField (
		orderBy = "keyword")
	Set<ChatSchemeKeywordRec> chatSchemeKeywords =
		new TreeSet<ChatSchemeKeywordRec> ();

	@CollectionField
	Set<ChatAffiliateRec> chatAffiliates =
		new TreeSet<ChatAffiliateRec> ();

	@CollectionField (
		key = "parent_chat_scheme_id")
	Set<ChatSchemeMapRec> chatSchemeMaps =
		new TreeSet<ChatSchemeMapRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatSchemeRec> otherRecord) {

		ChatSchemeRec other =
			(ChatSchemeRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChat (),
				other.getChat ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}