package wbs.psychic.user.core.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (chain = true)
@Data
public
class PsychicUserSearch {

	Integer id;

	String code;
	String number;

}
