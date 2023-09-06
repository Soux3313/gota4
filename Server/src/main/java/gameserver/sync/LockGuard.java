package gameserver.sync;

/**
 * the class produced by {@link OwningLock<T>.lock}
 * if you own one of these objects you can be sure that the underlying resource
 * has been locked, and therefore you are the unique(*) owner of the resource
 *
 * (*) warning: the implementation of OwningLock technically permits partial ownership (so read only ownership)
 * 		of the resource by using a {@link java.util.concurrent.locks.ReadWriteLock}, but for simplicity
 * 		this property is never used anywhere in the implementation
 *
 * @param <T> the type of the owned resource
 */
public class LockGuard<T> implements AutoCloseable {
	private OwningLock<T> parent;

	LockGuard(OwningLock<T> parent) {
		this.parent = parent;
	}

	/**
	 * returns the underlying resource that `this` `LockGuard` owns
	 *
	 * @return the underlying resource
	 * @throws IllegalMonitorStateException if this LockGuard does not own the reference
	 */
	public T get() throws IllegalMonitorStateException {
		if (this.parent != null) {
			return parent.resource;
		} else {
			throw new IllegalMonitorStateException("tried to get value from unlocked/non-owning LockGuard");
		}
	}

	/**
	 * unlocks the underlying lock
	 */
	public void unlock() {
		if (this.parent != null) {
			this.parent.unlock();
			this.parent = null;
		}
	}

	/**
	 * unlocks the underlying lock
	 */
	@Override
	public void close() {
		this.unlock();
	}
}
