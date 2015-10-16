package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.capitalise;

import java.io.IOException;

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
import wbs.framework.entity.meta.AssociativeCollectionSpec;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("associativeCollectionWriter")
@ModelWriter
public
class AssociativeCollectionWriter {

	// dependencies

	@Inject
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	AssociativeCollectionSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

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

		javaWriter.writeFormat (
			"\t@LinkField (\n");

		javaWriter.writeFormat (
			"\t\ttable = \"%s\")\n",
			spec.tableName ());

		javaWriter.writeFormat (
			"\tSet<%s.model.%sRec> %ss =\n",
			fieldTypePlugin.packageName (),
			capitalise (spec.typeName ()),
			spec.typeName ());

		javaWriter.writeFormat (
			"\t\tnew LinkedHashSet<%s.model.%sRec> ();\n",
			fieldTypePlugin.packageName (),
			capitalise (spec.typeName ()));

		javaWriter.writeFormat (
			"\n");

	}

}
