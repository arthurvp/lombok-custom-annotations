class LoggerJBossLog {
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(LoggerJBossLog.class);
}
class LoggerJBossLogWithImport {
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(LoggerJBossLogWithImport.class);
}
class LoggerJBossLogOuter {
	static class Inner {
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		@lombok.Generated
		private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(Inner.class);
	}
}

class LoggerJBossLogWithDifferentLoggerName {
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger("DifferentLogger");
}