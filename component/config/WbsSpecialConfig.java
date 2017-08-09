package wbs.framework.component.config;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class WbsSpecialConfig {

	Boolean assumeNegativeCache = false;

}
