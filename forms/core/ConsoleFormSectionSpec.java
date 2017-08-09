package wbs.console.forms.core;

import static wbs.utils.collection.CollectionUtils.emptyList;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name")
@ToString (of = "name")
@DataClass ("section")
@PrototypeComponent ("consoleMultiFormTypeSectionSpec")
public
class ConsoleFormSectionSpec
	implements ConsoleSpec {

	// tree

	@DataParent
	ConsoleFormSpec form;

	// attributes

	@DataAttribute (
		required = true)
	String name;

	// children

	@DataChildren (
		direct = true)
	List <Object> fields =
		emptyList ();

}
