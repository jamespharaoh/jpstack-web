package wbs.framework.schema.builder;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.entity.model.Model;
import wbs.framework.logging.TaskLogger;
import wbs.framework.schema.model.Schema;
import wbs.framework.schema.model.SchemaTable;

@Accessors (fluent = true)
@PrototypeComponent ("schemaFromAnnotations")
public
class SchemaFromModel {

	@Inject
	Provider<SchemaTableFromModel> schemaTableFromModel;

	@Getter @Setter
	TaskLogger taskLog;

	@Getter @Setter
	Map<String,List<String>> enumTypes;

	@Getter @Setter
	Map<Class<?>,Model> modelsByClass;

	public
	Schema build () {

		Schema schema =
			new Schema ()
				.enumTypes (enumTypes);

		for (Model model
				: modelsByClass.values ()) {

			SchemaTable schemaTable =
				schemaTableFromModel.get ()
					.taskLog (taskLog)
					.modelsByClass (modelsByClass)
					.model (model)
					.build ();

			if (schemaTable == null)
				continue;

			schema.addTable (
				schemaTable);

		}

		return schema;

	}

}
