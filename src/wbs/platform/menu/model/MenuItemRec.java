package wbs.platform.menu.model;

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
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class MenuItemRec
	implements MajorRecord<MenuItemRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	MenuGroupRec menuGroup;

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
	String label;

	@SimpleField
	String targetPath;

	@SimpleField
	String targetFrame;

	// compare to

	@Override
	public
	int compareTo (
			Record<MenuItemRec> otherRecord) {

		MenuItemRec other =
			(MenuItemRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getMenuGroup (),
				other.getMenuGroup ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
