package wbs.platform.object.criteria;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.core.ConsoleHelper;
import wbs.console.module.ConsoleModuleData;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.record.Record;
import wbs.utils.etc.PropertyUtils;

@Accessors (fluent = true)
@DataClass ("where-not-null")
@PrototypeComponent ("whereNotNullCriteriaSpec")
@ConsoleModuleData
public
class WhereNotNullCriteriaSpec
	implements CriteriaSpec {

	@DataAttribute (
		name = "field",
		required = true)
	@Getter @Setter
	String fieldName;

	@Override
	public
	boolean evaluate (
			ConsoleHelper<?> objectHelper,
			Record<?> object) {

		Object fieldValue =
			PropertyUtils.getProperty (
				object,
				fieldName);

		return fieldValue != null;

	}

}
