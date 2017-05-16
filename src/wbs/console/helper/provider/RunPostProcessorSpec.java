package wbs.console.helper.provider;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("run-post-processor")
@PrototypeComponent ("runPostProcessorSpec")
public
class RunPostProcessorSpec
	implements ConsoleSpec {

	@DataAttribute
	String name;

}
