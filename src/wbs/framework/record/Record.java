package wbs.framework.record;

public
interface Record<Type extends Record<Type>>
	extends
		Comparable<Record<Type>>,
		IdObject {

}
