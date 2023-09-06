package gameserver.logger;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * logging facility, will write to stdout/stderr depending on the loglevel
 * the logging is inspired by the systemd logging specification but does not
 * follow it completely
 */
public class Logger {

	/**
	 * the formatter to format dates
	 */
	private static final SimpleDateFormat datefmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * determines if the logger should also log the current date time
	 * true  => don't log date-time
	 * false => log date-time
	 */
	private final boolean isManagedDaemon;

	/**
	 * determines if the logger should log `DEBUG`-level log-entries
	 */
	private final boolean logDebug;

	/**
	 * determines if the logger should log `TRAFFIC`-level log-entries
	 */
	private final boolean logTraffic;

	/**
	 * format string used internally to format log-entries
	 */
	private final String fmt;

	/**
	 * constructs a Logger by explicitly specifiying if this process is a daemon
	 *
	 * @param logDebug specifies if the logger should log `DEGUG` level messages, if false they will be ignored
	 * @param logTraffic specifies if the logger should log `TRAFFIC` level messages, if false they will be ignored
	 * @param isManagedDaemon pass true if this process is managed by e.g. systemd
	 */
	public Logger(boolean logDebug, boolean logTraffic, boolean isManagedDaemon) {
		this.logDebug = logDebug;
		this.logTraffic = logTraffic;
		this.isManagedDaemon = isManagedDaemon;
		this.fmt = isManagedDaemon ? "%-9s %s" : "%-9s [%s] %s";
	}

	/**
	 * constructs a logger for a process not being managed by a service manager
	 * this is the default constructor that should probably be used
	 */
	public Logger(boolean logDebug, boolean logTraffic) {
		this(logDebug, logTraffic, false);
	}

	/**
	 * much like System.out.printf, but prepends loglevel
	 * and date-time (if daemon)
	 *
	 * @param level the priority of this message
	 * @param text the fmt string
	 * @param fmtargs the args to insert into `text`
	 *
	 * @return this
	 */
	public synchronized Logger log(LogLevel level, String text, Object... fmtargs) {

		// don't log debug level if not in debug mode
		if (!this.logDebug && level == LogLevel.DEBUG) {
			return this;
		}

		// don't log traffic if not in traffic log mode
		if (!this.logTraffic && level == LogLevel.TRAFFIC) {
			return this;
		}

		String msg = this.doFmt(level.toString(), text, fmtargs);

		switch (level) {
			case ERR:
			case WARNING:
				System.err.println(msg);
				break;
			case NOTICE:
			case INFO:
			case DEBUG:
			case TRAFFIC:
				System.out.println(msg);
				break;
		}
		return this;
	}

	/**
	 * formatts a specified text as a log-entry
	 *
	 * @param level the log-level of the entry
	 * @param text the specified fmt or base text (simmilar to printf)
	 * @param fmtargs the arguments that shall be substituted into `text`
	 * @return a log-entry with the specified contents
	 */
	public synchronized String doFmt(String level, String text, Object... fmtargs) {
		if (isManagedDaemon) {
			return String.format(fmt,
					String.format("<%s>", level),
					String.format(text, fmtargs));
		} else {
			return String.format(fmt,
					String.format("<%s>", level),
					datefmt.format(new Date()),
					String.format(text, fmtargs));
		}
	}

	/**
	 * logs the error to stderr and then exits the program because
	 * there is no way to recover from a situation.
	 * everything else is the same as all the other log methods.
	 *
	 * @param exitcode the code with which the program should exit
	 */
	public synchronized void panic(int exitcode, String text, Object... fmtargs) {
		String msg = this.doFmt("PANIC", text, fmtargs);
		System.err.println(msg);
		System.exit(exitcode);
	}

	/**
	 * convenience method for `this.log` with prespecified loglevel `ERROR`
	 */
	public synchronized Logger err(String text, Object... fmtargs) {
		return this.log(LogLevel.ERR, text, fmtargs);
	}

	/**
	 * convenience method for `this.log` with prespecified loglevel `WARNING`
	 */
	public synchronized Logger warning(String text, Object... fmtargs) {
		return this.log(LogLevel.WARNING, text, fmtargs);
	}

	/**
	 * convenience method for `this.log` with prespecified loglevel `NOTICE`
	 */
	public synchronized Logger notice(String text, Object... fmtargs) {
		return this.log(LogLevel.NOTICE, text, fmtargs);
	}

	/**
	 * convenience method for `this.log` with prespecified loglevel `INFO`
	 */
	public synchronized Logger info(String text, Object... fmtargs) {
		return this.log(LogLevel.INFO, text, fmtargs);
	}

	/**
	 * convenience method for `this.log` with prespecified loglevel `DEBUG`
	 */
	public synchronized Logger debug(String text, Object... fmtargs) {
		return this.log(LogLevel.DEBUG, text, fmtargs);
	}


	/**
	 * logs incoming http requests
	 *
	 * @param method the method of the request
	 * @param path the path on which the request arrived
	 * @param body the body of the request (if any)
	 * @return this
	 */
	public synchronized Logger trafficInboundRequest(String method, String path, String body) {
		return this.log(LogLevel.TRAFFIC,
				"->: %s %s :: %s",
				method,
				path,
				body);
	}

	/**
	 * logs incoming http responses
	 *
	 * @param code the response status code
	 * @param body the response body
	 * @return this
	 */
	public synchronized Logger trafficInboundResponse(int code, String body) {
		return this.log(LogLevel.TRAFFIC,
				"->: %d :: %s",
				code,
				body);
	}

	/**
	 * logs outgoing http responses
	 *
	 * @param code the status code of the response
	 * @param body the body of the response
	 * @return this
	 */
	public synchronized Logger trafficOutboundResponse(int code, String body) {
		return this.log(LogLevel.TRAFFIC,
				"<-: %d :: %s",
				code,
				body);
	}

	/**
	 * logs outgoing http requests
	 *
	 * @param method the request method used
	 * @param path the path on which the request was sent
	 * @param body the body of the request
	 * @return this
	 */
	public synchronized Logger trafficOutboundRequest(String method, String path, String body) {
		return this.log(LogLevel.TRAFFIC,
				"<-: %s %s :: %s",
				method,
				path,
				body);
	}

}
