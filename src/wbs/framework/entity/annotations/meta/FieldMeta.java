package wbs.framework.entity.annotations.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import wbs.framework.entity.model.ModelFieldType;

@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.ANNOTATION_TYPE)
public
@interface FieldMeta {

	public
	ModelFieldType modelFieldType ();

	public
	boolean treeParent ()
	default false;

	public
	boolean treeIdentity ()
	default false;

}
