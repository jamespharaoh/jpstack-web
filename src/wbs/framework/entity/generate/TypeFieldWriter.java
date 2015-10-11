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
import wbs.framework.entity.meta.TypeFieldSpec;

@PrototypeComponent ("typeFieldWriter")
@ModelWriter
public
class TypeFieldWriter {

	// dependencies

	@Inject
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	TypeFieldSpec spec;

	@BuilderTarget
	Writer javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder)
		throws IOException {

		String fieldTypeName =
			stringFormat (
				"%sType",
				parent.name ());

		PluginModelSpec fieldTypePluginModel =
			pluginManager.pluginModelsByName ().get (
				fieldTypeName);

		PluginSpec fieldTypePlugin =
			fieldTypePluginModel.plugin ();

		javaWriter.write (
			stringFormat (

				"\t@TypeField\n",

				"\t%s.model.%sRec type;\n",
				fieldTypePlugin.packageName (),
				capitalise (fieldTypeName),

				"\n"));

	}

}
