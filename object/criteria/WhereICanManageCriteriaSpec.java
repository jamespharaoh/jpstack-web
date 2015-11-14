package wbs.platform.object.criteria;

import javax.inject.Inject;

import lombok.experimental.Accessors;

import wbs.console.helper.ConsoleHelper;
import wbs.console.module.ConsoleModuleData;
import wbs.console.priv.PrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.record.Record;

@Accessors (fluent = true)
@DataClass ("where-i-can-manage")
@PrototypeComponent ("whereICanManageCriteriaSpec")
@ConsoleModuleData
public
class WhereICanManageCriteriaSpec
	implements CriteriaSpec {

	@Inject
	PrivChecker privChecker;

	@Override
	public
	boolean evaluate (
			ConsoleHelper<?> objectHelper,
			Record<?> object) {

		return privChecker.can (
			object,
			"manage");

	}

}
