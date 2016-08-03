package wbs.framework.fixtures;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("test-account")
public
class TestAccountSpec {

	@DataParent
	TestAccountsSpec testAccounts;

	@DataAttribute
	String type;

	@DataAttribute
	String name;

	@DataChildren (
		direct = true,
		childElement = "param",
		keyAttribute = "name",
		valueAttribute = "value")
	Map<String,String> params =
		new LinkedHashMap<String,String> ();

}
