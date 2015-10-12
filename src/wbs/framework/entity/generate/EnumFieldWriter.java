package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;

import java.io.IOException;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.scaffold.PluginEnumTypeSpec;
import wbs.framework.application.scaffold.PluginManager;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.EnumFieldSpec;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("enumFieldWriter")
@ModelWriter
public
class EnumFieldWriter {

	// dependencies

	@Inject
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	EnumFieldSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder)
		throws IOException {

		PluginEnumTypeSpec fieldTypePluginEnumType =
			pluginManager.pluginEnumTypesByName ().get (
				spec.typeName ());

		PluginSpec fieldTypePlugin =
			fieldTypePluginEnumType.plugin ();

		if (ifNull (spec.nullable (), false)) {

			javaWriter.write (

				"\t@SimpleField (\n",

				"\t\tnullable = true)\n");

		} else {

			javaWriter.write (

				"\t@SimpleField\n");

		}

		if (spec.defaultValue () != null) {

			javaWriter.write (
				"\t%s.model.%s %s =\n",
				fieldTypePlugin.packageName (),
				capitalise (
					spec.typeName ()),
				spec.name ());

			javaWriter.write (
				 "\t\t%s.model.%s.%s;\n",
				fieldTypePlugin.packageName (),
				capitalise (
					spec.typeName ()),
				spec.defaultValue ());

		} else {

			javaWriter.write (

				"\t%s.model.%s %s;\n",
				fieldTypePlugin.packageName (),
				capitalise (
					spec.typeName ()),
				spec.name ());

		}

		javaWriter.write (

			"\n");

	}

}
