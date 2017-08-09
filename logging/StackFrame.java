package wbs.framework.logging;

import static wbs.utils.string.StringUtils.stringFormatArray;

public
class StackFrame {

	static
	ThreadLocal <StackFrame> stackFrameThreadLocal =
		new ThreadLocal<> ();

	StackFrame parentFrame;
	String [] args;

	public
	void close () {

		if (stackFrameThreadLocal.get () != this)
			throw new RuntimeException ();

		stackFrameThreadLocal.set (
			parentFrame);

	}

	public
	void printStackTrace () {

		System.out.println (
			stringFormatArray (
				args));

		parentFrame.printStackTrace ();

	}

	public
	void rethrow (
			Exception exception) {

	}

	public static
	StackFrame enter (
			String ... args) {

		StackFrame oldFrame =
			stackFrameThreadLocal.get ();

		StackFrame newFrame =
			new StackFrame ();

		newFrame.parentFrame =
			oldFrame;

		newFrame.args =
			args;

		stackFrameThreadLocal.set (
			newFrame);

		return newFrame;

	}

}
