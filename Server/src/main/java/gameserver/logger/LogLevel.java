package gameserver.logger;

/**
 * LogLevel inspired by the systemd priority level specification
 * leaving out
 * 		EMERG:    because this is not the kernel
 * 		ALERT:    because this is not a vital subsystem of the kernel
 *		CRITICAL: because this is not a primary system application like x11
 * putting in
 * 		TRAFFIC:  to have a specified log level for http traffic so you can filter it out or find it more easily
 *
 * see `https://wiki.archlinux.org/index.php/Systemd/Journal` for more information
 */
public enum LogLevel {
	ERR,     // error conditions
	WARNING, // may indicate that an error might occur if not corrected
	NOTICE,  // unusual but not error
	INFO,    // normal operational messages
	DEBUG,   // information for debugging
	TRAFFIC, // information on http traffic
}
