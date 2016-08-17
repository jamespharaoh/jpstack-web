package wbs.platform.media.model;

import java.util.List;

public
interface ContentDaoMethods {

	List <ContentRec> findByShortHash (
			Long shortHash);

}