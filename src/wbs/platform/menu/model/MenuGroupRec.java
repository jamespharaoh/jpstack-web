package wbs.platform.menu.model;

import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class MenuGroupRec
	implements MajorRecord<MenuGroupRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@CodeField
	String code;

	// details

	@SimpleField
	String label = "";

	@SimpleField
	Integer order = 0;

	// children

	@CollectionField (
		key = "group_id",
		orderBy = "label")
	Set<MenuRec> menus;

	// compare to

	@Override
	public
	int compareTo (
			Record<MenuGroupRec> otherRecord) {

		MenuGroupRec other =
			(MenuGroupRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getOrder (),
				other.getOrder ())

			.append (
				getLabel (),
				other.getLabel ())

			.toComparison ();

	}

}
