package wbs.platform.affiliate.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.model.ModelMetaData;

@Accessors (fluent = true)
@Data
@DataClass ("affiliate-types")
@PrototypeComponent ("affiliateTypesSpec")
@ModelMetaData
public
class AffiliateTypesSpec {

	@DataChildren (
		direct = true)
	List<AffiliateTypeSpec> affiliateTypes =
		new ArrayList<AffiliateTypeSpec> ();

}
