package wbs.platform.exception.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode
@ToString
public
class ExceptionLogSearch {

	Boolean alert;
	Boolean fatal;

	Integer typeId;
	Integer userId;

	Order order =
		Order.timestampDesc;

	Integer maxResults;

	public static
	enum Order {
		timestampDesc
	}

}
