package wbs.framework.entity.meta.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name")
@ToString (of = "name")
@DataClass ("model-meta")
@PrototypeComponent ("modelMetaSpec")
@ModelMetaData
public
class ModelMetaSpec {

	@DataParent
	PluginSpec plugin;

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String oldName;

	@DataAttribute (
		required = true)
	ModelMetaType type;

	@DataAttribute (
		name = "table")
	String tableName;

	@DataAttribute
	Boolean create;

	@DataAttribute
	Boolean mutable;

	// children

	@DataChildren (
		childrenElement = "implements-interfaces")
	List <ModelImplementsInterfaceSpec> implementsInterfaces =
		new ArrayList<> ();

	@DataChildren (
		childrenElement = "fields")
	List <ModelFieldSpec> fields =
		new ArrayList<> ();

	@DataChildren (
		childrenElement = "collections")
	List <ModelCollectionSpec> collections =
		new ArrayList<> ();

	@DataChildren (
		direct = true,
		excludeChildren = { "fields", "collections" })
	List <Object> children =
		new ArrayList<> ();

}
