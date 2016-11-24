package wbs.platform.object.criteria;

import lombok.experimental.Accessors;

import wbs.console.helper.core.ConsoleHelper;
import wbs.console.module.ConsoleModuleData;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.record.Record;
import wbs.utils.etc.PropertyUtils;

@Accessors (fluent = true)
@DataClass ("where-not-deleted")
@PrototypeComponent ("whereNotDeletedCriteriaSpec")
@ConsoleModuleData
public
class WhereNotDeletedCriteriaSpec
	implements CriteriaSpec {

	@Override
	public
	boolean evaluate (
			ConsoleHelper<?> objectHelper,
			Record<?> object) {

		Boolean deleted =
			(Boolean)
			PropertyUtils.propertyGetAuto (
				object,
				"deleted");

		return ! deleted;

	}

}
