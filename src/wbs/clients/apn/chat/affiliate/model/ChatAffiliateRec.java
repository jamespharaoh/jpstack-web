package wbs.clients.apn.chat.affiliate.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeKeywordRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class ChatAffiliateRec
	implements MajorRecord<ChatAffiliateRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatSchemeRec chatScheme;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String description = "";

	@DeletedField
	Boolean deleted = false;

	// settings

	@SimpleField (
		nullable = true)
	String brandName = null;

	// statistics

	@SimpleField
	Integer numUsers = 0;

	// children

	@CollectionField (
		key = "join_chat_affiliate_id")
	Set<ChatSchemeKeywordRec> chatSchemeKeywords =
		new HashSet<ChatSchemeKeywordRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatAffiliateRec> otherRecord) {

		ChatAffiliateRec other =
			(ChatAffiliateRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChatScheme (),
				other.getChatScheme ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

	// dao methods

		public static
		interface ChatAffiliateDaoMethods {

			List<ChatAffiliateRec> find (
					ChatRec chat);

		}

}
