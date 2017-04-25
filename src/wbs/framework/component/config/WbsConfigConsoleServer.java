package wbs.framework.component.config;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("console-server")
@PrototypeComponent ("wbsConfigConsoleServer")
public
class WbsConfigConsoleServer {

	@DataAttribute (
		required = true)
	Long listenPort;

}
