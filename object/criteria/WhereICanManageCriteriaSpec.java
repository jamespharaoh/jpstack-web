package wbs.platform.object.criteria;

import javax.inject.Inject;

import lombok.experimental.Accessors;

import wbs.console.helper.ConsoleHelper;
import wbs.console.module.ConsoleModuleData;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.record.Record;

@Accessors (fluent = true)
@DataClass ("where-i-can-manage")
@PrototypeComponent ("whereICanManageCriteriaSpec")
@ConsoleModuleData
public
class WhereICanManageCriteriaSpec
	implements CriteriaSpec {

	@Inject
	UserPrivChecker privChecker;

	@Override
	public
	boolean evaluate (
			ConsoleHelper<?> objectHelper,
			Record<?> object) {

		return privChecker.canRecursive (
			object,
			"manage");

	}

}
