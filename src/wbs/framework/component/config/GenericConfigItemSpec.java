package wbs.framework.component.config;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataElementAttributes;
import wbs.framework.data.annotations.DataElementName;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass (element = true)
public
class GenericConfigItemSpec {

	@DataParent
	GenericConfigSpec testAccounts;

	@DataElementName
	String type;

	@DataElementAttributes
	Map <String, String> params =
		new LinkedHashMap <String, String> ();

}
