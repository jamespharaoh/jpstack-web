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
import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.framework.entity.meta.cachedview.CachedViewSpec;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name")
@ToString (of = "name")
@DataClass ("model-meta")
@PrototypeComponent ("modelMetaSpec")
public
class ModelMetaSpec
	implements ModelDataSpec {

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

	@DataChild
	ModelPartitioningSpec partitioning;

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

	@DataChild
	CachedViewSpec cachedView;

	@DataChildren (
		direct = true,
		excludeChildren = { "fields", "collections", "cached-view" })
	List <Object> children =
		new ArrayList<> ();

}
