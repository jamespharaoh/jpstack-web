package wbs.framework.entity.generate.collections;

import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.naivePluralise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

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
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.generate.fields.ModelFieldWriterContext;
import wbs.framework.entity.generate.fields.ModelFieldWriterTarget;
import wbs.framework.entity.meta.ChildrenMappingSpec;
import wbs.framework.entity.meta.ModelMetaLoader;

@PrototypeComponent ("childrenMappingWriter")
@ModelWriter
public
class ChildrenMappingWriter {

	// dependencies

	@Inject
	PluginManager pluginManager;

	@Inject
	ModelMetaLoader modelMetaLoader;

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	ChildrenMappingSpec spec;

	@BuilderTarget
	ModelFieldWriterTarget target;

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

		if (
			isNull (
				fieldTypePluginModel)
		) {

			throw new RuntimeException (
				stringFormat (
					"Field '%s.%s' ",
					context.modelMeta ().name (),
					fieldName,
					"has type '%s' ",
					spec.typeName (),
					"which does not exist"));


		}

		PluginSpec fieldTypePlugin =
			fieldTypePluginModel.plugin ();

		String fullFieldTypeName =
			stringFormat (
				"%s.model.%sRec",
				fieldTypePlugin.packageName (),
				capitalise (
					spec.typeName ()));

		Class <?> keyType =
			mapTypeClasses.get (
				spec.mapType ());

		// write field

		new JavaPropertyWriter ()

			.thisClassNameFormat (
				"%s.model.%s",
				context.modelMeta ().plugin ().packageName (),
				context.recordClassName ())

			.typeName (
				imports ->
					stringFormat (
						"%s <%s, %s>",
						imports.register (
							Map.class),
						imports.register (
							keyType),
						imports.register (
							fullFieldTypeName)))

			.propertyName (
				fieldName)

			.defaultValue (
				imports ->
					stringFormat (
						"new %s <%s, %s> ()",
						imports.register (
							LinkedHashMap.class),
						imports.register (
							keyType),
						imports.register (
							fullFieldTypeName)))

			.writeBlock (
				target.imports (),
				target.formatWriter ());


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
