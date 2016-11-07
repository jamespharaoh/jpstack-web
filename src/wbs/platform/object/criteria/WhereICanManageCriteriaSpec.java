package wbs.platform.object.criteria;

import lombok.experimental.Accessors;

import wbs.console.helper.core.ConsoleHelper;
import wbs.console.module.ConsoleModuleData;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.record.Record;

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
			ConsoleHelper <?> objectHelper,
			Record <?> object) {

		return privChecker.canRecursive (
			object,
			"manage");

	}

}
