package shn.product.model;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class ShnProductSearch
	implements Serializable {

	Long sliceId;

	String name;
	String description;

	Boolean deleted;

	String publicTitle;
	String publicDescription;

	Long productTypeId;

	String sku;

}
