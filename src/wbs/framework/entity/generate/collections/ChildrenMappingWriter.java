package wbs.framework.entity.generate.collections;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.naivePluralise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

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
import wbs.framework.entity.meta.ChildrenMappingSpec;
import wbs.framework.entity.meta.ModelMetaLoader;
import wbs.framework.utils.formatwriter.FormatWriter;

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
	FormatWriter javaWriter;

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
				"%s",
				context.recordClassName ())

			.typeNameFormat (
				"Map <%s, %s>",
				keyType.getSimpleName (),
				fullFieldTypeName)

			.propertyNameFormat (
				"%s",
				fieldName)

			.defaultValueFormat (
				"new LinkedHashMap <%s, %s> ()",
				keyType.getSimpleName (),
				fullFieldTypeName)

			.write (
				javaWriter,
				"\t");


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
