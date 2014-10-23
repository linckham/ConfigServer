package com.cmbc.configserver.remoting.common;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * @author tongchuan.lin<linckham@gmail.com>
 * @since 2014/10/17 3:01:22PM
 */
public class SemaphoreReleaseOnlyOnce {
	private final AtomicBoolean released = new AtomicBoolean(false);
	private final Semaphore semaphore;

	public SemaphoreReleaseOnlyOnce(Semaphore semaphore) {
		this.semaphore = semaphore;
	}

	public void release() {
		if (this.semaphore != null) {
			if (this.released.compareAndSet(false, true)) {
				this.semaphore.release();
			}
		}
	}

	public Semaphore getSemaphore() {
		return semaphore;
	}
}