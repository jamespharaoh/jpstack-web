package wbs.framework.data.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.FIELD)
public
@interface DataChildren {

	boolean direct ()
	default false;

	String childrenElement ()
	default "";

	String childElement ()
	default "";

	String[] excludeChildren ()
	default {};

	String surrogateParent ()
	default "";

	String keyAttribute ()
	default "";

	String valueAttribute ()
	default "";

}
