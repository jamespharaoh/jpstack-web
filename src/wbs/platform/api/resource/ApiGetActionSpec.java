package wbs.platform.api.resource;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.api.module.ApiModuleData;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name")
@ToString (of = "name")
@DataClass ("get-action")
@PrototypeComponent ("apiGetActionSpec")
@ApiModuleData
public
class ApiGetActionSpec {

	@DataAttribute (
		required = true)
	String name;

}
