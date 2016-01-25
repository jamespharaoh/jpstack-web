package wbs.applications.imchat.model;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.Interval;

@Accessors (fluent = true)
@Data
public
class ImChatMessageSearch
	implements Serializable {

	Integer imChatId;

	Interval timestamp;

}
