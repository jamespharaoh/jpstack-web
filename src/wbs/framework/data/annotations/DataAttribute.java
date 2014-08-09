package wbs.framework.data.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.FIELD)
public
@interface DataAttribute {

	String value ()
	default "";

	String collection ()
	default "";

	boolean required ()
	default false;

}
