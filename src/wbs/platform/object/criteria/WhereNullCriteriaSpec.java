package wbs.platform.object.criteria;

import static wbs.utils.etc.PropertyUtils.propertyGetAuto;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.core.ConsoleHelper;
import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@DataClass ("where-null")
@PrototypeComponent ("whereNullCriteriaSpec")
@ConsoleModuleData
public
class WhereNullCriteriaSpec
	implements CriteriaSpec {

	@DataAttribute (
		name = "field",
		required = true)
	@Getter @Setter
	String fieldName;

	@Override
	public
	boolean evaluate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleHelper <?> objectHelper,
			@NonNull Record <?> object) {

		Object fieldValue =
			propertyGetAuto (
				object,
				fieldName);

		return fieldValue == null;

	}

}
