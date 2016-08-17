package wbs.platform.exception.model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode
@ToString
public
class ExceptionLogSearch
	implements Serializable {

	Boolean alert;
	Boolean fatal;

	Long typeId;
	Long userId;

	Order order =
		Order.timestampDesc;

	Long maxResults;

	public static
	enum Order {
		timestampDesc
	}

}
