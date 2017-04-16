package wbs.platform.object.criteria;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.helper.core.ConsoleHelper;
import wbs.console.module.ConsoleModuleData;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@DataClass ("where-i-can-manage")
@PrototypeComponent ("whereICanManageCriteriaSpec")
@ConsoleModuleData
public
class WhereICanManageCriteriaSpec
	implements CriteriaSpec {

	// singleton dependencies

	@SingletonDependency
	UserPrivChecker privChecker;

	// implementation

	@Override
	public
	boolean evaluate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleHelper <?> objectHelper,
			@NonNull Record <?> object) {

		return privChecker.canRecursive (
			parentTaskLogger,
			object,
			"manage");

	}

}
