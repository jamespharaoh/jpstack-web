package wbs.platform.menu.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
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
class MenuRec
	implements MajorRecord<MenuRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField (
		column = "group_id")
	MenuGroupRec menuGroup;

	@CodeField
	String code;

	// details

	@SimpleField
	String label = "";

	@SimpleField
	String path = "";

	@SimpleField
	String target = "main";

	// compare to

	@Override
	public
	int compareTo (
			Record<MenuRec> otherRecord) {

		MenuRec other =
			(MenuRec) otherRecord;

		return new CompareToBuilder ()
			.append (getMenuGroup (), other.getMenuGroup ())
			.append (getCode (), other.getCode ())
			.toComparison ();

	}

}
