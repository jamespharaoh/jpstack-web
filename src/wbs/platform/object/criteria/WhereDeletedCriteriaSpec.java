package wbs.platform.object.criteria;

import lombok.experimental.Accessors;

import wbs.console.helper.ConsoleHelper;
import wbs.console.module.ConsoleModuleData;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.record.Record;
import wbs.framework.utils.etc.BeanLogic;

@Accessors (fluent = true)
@DataClass ("where-deleted")
@PrototypeComponent ("whereDeletedCriteriaSpec")
@ConsoleModuleData
public
class WhereDeletedCriteriaSpec
	implements CriteriaSpec {

	@Override
	public
	boolean evaluate (
			ConsoleHelper<?> objectHelper,
			Record<?> object) {

		Boolean deleted =
			(Boolean)
			BeanLogic.getProperty (
				object,
				"deleted");

		return deleted;

	}

}
