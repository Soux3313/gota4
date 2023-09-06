package gameserver.sync;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

/**
 * A lock that owns its underlying resource. It is semantically incorrect to have a reference
 * to the underlying object that was not obtained through OwningLock's public interface, the java
 * compiler can't enforce this so you have to be careful to not do this. Failure to ensure this
 * breaks the syncronization.
 *
 * @param <T> the type of the owned resource
 */
public class OwningLock<T> {
	final Lock lock;
	final T resource;

	/**
	 * constructs a owning lock out of a lock and the resource
	 * this implies that no other entity owns either `lock` or `resource`
	 *
	 * @param lock the specific lock {@link OwningLock} should manage (it is assumed to be unlocked when calling this ctor)
	 * @param resource the resource to manage, this takes ownership and therefore no other entity may refer to it directly
	 */
	public OwningLock(Lock lock, T resource) {
		this.lock = lock;
		this.resource = resource;
	}

	/**
	 * unlocks the lock
	 * **this is intentionally package private since unlocking is done by {@link LockGuard}**
	 */
	void unlock() {
		this.lock.unlock();
	}

	/**
	 * aquires the lock to the underlying resource
	 *
	 * @return the LockGuard representing unique(*) ownership of the underlying object
	 * @throws IllegalMonitorStateException if a thread tries to get more than one lock on the resource
	 *
	 * (*) see documentation of {@link LockGuard} for more information
	 */
	public LockGuard<T> lock() {
		this.lock.lock();
		return new LockGuard<>(this);
	}
}
