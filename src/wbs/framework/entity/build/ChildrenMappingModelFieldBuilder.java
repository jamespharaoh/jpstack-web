package wbs.framework.entity.build;

import static wbs.framework.utils.etc.Misc.doesNotContain;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.camelToSpaces;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.naivePluralise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.TypeUtils.classForNameRequired;

import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.reflect.TypeUtils;

import com.google.common.collect.ImmutableMap;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.scaffold.PluginManager;
import wbs.framework.application.scaffold.PluginModelSpec;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.ChildrenMappingSpec;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;

@PrototypeComponent ("childrenMappingModelFieldBuilder")
@ModelBuilder
public
class ChildrenMappingModelFieldBuilder {

	// dependencies

	@Inject
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelFieldBuilderContext context;

	@BuilderSource
	ChildrenMappingSpec spec;

	@BuilderTarget
	ModelFieldBuilderTarget target;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String fieldName =
			ifNull (
				spec.name (),
				naivePluralise (
					spec.typeName ()));

		PluginModelSpec fieldTypePluginModel =
			pluginManager.pluginModelsByName ().get (
				spec.typeName ());

		PluginSpec fieldTypePlugin =
			fieldTypePluginModel.plugin ();

		String fullFieldTypeName =
			stringFormat (
				"%s.model.%sRec",
				fieldTypePlugin.packageName (),
				capitalise (
					spec.typeName ()));

		Class <?> fieldTypeClass =
			classForNameRequired (
				fullFieldTypeName);

		if (
			doesNotContain (
				mapTypeClasses.keySet (),
				spec.mapType ())
		) {

			throw new RuntimeException (
				stringFormat (
					"Invalid map type %s ",
					spec.mapType (),
					"for model child field %s.%s",
					context.modelMeta.name (),
					fieldName));

		}

		Class <?> mapTypeClass =
			mapTypeClasses.get (
				spec.mapType ());

		// create model field

		ModelField modelField =
			new ModelField ()

			.model (
				target.model ())

			.parentField (
				context.parentModelField ())

			.name (
				fieldName)

			.label (
				camelToSpaces (
					fieldName))

			.type (
				ModelFieldType.collection)

			.parent (
				false)

			.identity (
				false)

			.valueType (
				Map.class)

			.parameterizedType (
				TypeUtils.parameterize (
					Map.class,
					mapTypeClass,
					fieldTypeClass))

			.collectionKeyType (
				mapTypeClass)

			.collectionValueType (
				fieldTypeClass)

			.whereSql (
				spec.whereSql ())

			.orderSql (
				spec.orderSql ())

			.joinColumnName (
				spec.joinColumnName ())

			.mappingKeyColumnName (
				spec.mapColumnName ());

		// store field

		target.fields ().add (
			modelField);

		target.fieldsByName ().put (
			modelField.name (),
			modelField);

	}

	public final static
	Map <String, Class <?>> mapTypeClasses =
		ImmutableMap.<String, Class <?>> builder ()

		.put (
			"string",
			String.class)

		.put (
			"integer",
			Long.class)

		.build ();

}
