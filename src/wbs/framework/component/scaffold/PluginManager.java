package wbs.framework.component.scaffold;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.entity.record.Record;

@Accessors (fluent = true)
public
class PluginManager {

	// state

	@Getter @Setter
	List <PluginSpec> plugins;

	@Getter @Setter
	Map <String, PluginModelSpec> pluginModelsByName;

	@Getter @Setter
	Map <String, PluginEnumTypeSpec> pluginEnumTypesByName;

	@Getter @Setter
	Map <String, PluginCustomTypeSpec> pluginCustomTypesByName;

	// implementation

	public
	Class <? extends Record <?>> modelClass (
			@NonNull String modelName) {

		PluginModelSpec modelSpec =
			mapItemForKeyRequired (
				pluginModelsByName,
				hyphenToCamel (
					modelName));

		String modelClassName =
			stringFormat (
				"%s.model.%sRec",
				modelSpec.plugin ().packageName (),
				capitalise (
					modelSpec.name ()));

		return genericCastUnchecked (
			classForNameRequired (
				modelClassName));

	}

}
