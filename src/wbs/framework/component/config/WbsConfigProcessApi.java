package wbs.framework.component.config;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("process-api")
@PrototypeComponent ("wbsConfigProcessApi")
public
class WbsConfigProcessApi {

	@DataAttribute (
		required = true)
	Long listenPort;

}
