package shn.show.model;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.utils.time.TextualInterval;

@Accessors (fluent = true)
@Data
public
class ShnShowSearch
	implements Serializable {

	Long databaseId;
	Long showTypeId;

	String description;

	Boolean deleted;

	TextualInterval startTime;

}
