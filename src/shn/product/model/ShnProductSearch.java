package shn.product.model;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class ShnProductSearch
	implements Serializable {

	Long shnDatabaseId;

	Long productCategoryId;
	Long productSubCategoryId;

	String itemNumber;
	String description;

	Boolean deleted;
	Boolean active;

	String publicTitle;
	String publicDescription;

}
