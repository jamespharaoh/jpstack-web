package wbs.framework.entity.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("modelToXml")
public
class ModelToXml {

	@Getter @Setter
	Model model;

	public
	Document build () {

		Document document =
			DocumentHelper.createDocument ();

		return document;

	}

}
