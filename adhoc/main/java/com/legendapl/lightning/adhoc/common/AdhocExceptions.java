package com.legendapl.lightning.adhoc.common;

public class AdhocExceptions {

	public static class NoSuchOperationTypeException extends RuntimeException {
		private static final long serialVersionUID = -4999395804107998900L;
	}

	public static class NoSuchDataTypeException extends RuntimeException {
		private static final long serialVersionUID = 7086608625441668830L;
	}

	public static final class OutOfRangeException extends RuntimeException {
		private static final long serialVersionUID = -3225691482434686741L;
	}

	public static final class NotNumberException extends RuntimeException {
		private static final long serialVersionUID = -7658432654968203557L;
	}

	public static final class NotIntegerException extends RuntimeException {
		private static final long serialVersionUID = -5128787483326147890L;
	}
}
