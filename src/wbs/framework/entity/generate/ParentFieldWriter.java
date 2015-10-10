package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.Writer;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.scaffold.PluginManager;
import wbs.framework.application.scaffold.PluginModelSpec;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.meta.ParentFieldSpec;

@PrototypeComponent ("parentFieldWriter")
@ModelWriter
public
class ParentFieldWriter {

	// dependencies

	@Inject
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	ParentFieldSpec spec;

	@BuilderTarget
	Writer javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder)
		throws IOException {

		PluginModelSpec fieldTypePluginModel =
			pluginManager.pluginModelsByName ().get (
				spec.typeName ());

		PluginSpec fieldTypePlugin =
			fieldTypePluginModel.plugin ();

		javaWriter.write (
			stringFormat (

				"\t@ParentField\n",

				"\t%s.model.%sRec %s;\n",
				fieldTypePlugin.packageName (),
				capitalise (spec.typeName ()),
				spec.typeName (),

				"\n"));

	}

}
