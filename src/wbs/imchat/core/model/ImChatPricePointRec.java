package wbs.imchat.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public
class ImChatPricePointRec
	implements CommonRecord<ImChatPricePointRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ImChatRec imChat;

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
	Integer price;

	@SimpleField
	Integer value;

	// compare to

	@Override
	public
	int compareTo (
			Record<ImChatPricePointRec> otherRecord) {

		ImChatPricePointRec other =
			(ImChatPricePointRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getImChat (),
				other.getImChat ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
