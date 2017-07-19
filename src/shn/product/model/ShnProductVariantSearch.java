package shn.product.model;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.Range;

@Accessors (fluent = true)
@Data
public
class ShnProductVariantSearch
	implements Serializable {

	Long shnDatabaseId;

	Long productCategoryId;
	Long productSubCategoryId;

	String itemNumber;
	String description;

	Boolean deleted;
	Boolean active;

	String publicTitle;

	Range <Long> stockQuantity;

}
