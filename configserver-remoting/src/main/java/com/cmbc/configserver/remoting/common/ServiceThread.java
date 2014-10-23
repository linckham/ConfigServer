package com.cmbc.configserver.remoting.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ServiceThread implements Runnable {
	private static final Logger stlog = LoggerFactory
			.getLogger(RemotingHelper.RemotingLogName);
	// the executor thread
	protected final Thread thread;
	// the recycle time of the thread.the default value is 90s.
	private static final long JOIN_TIME = 90 * 1000;
	// whether is notified
	protected volatile boolean hasNotified = false;
	// whether is stopped
	protected volatile boolean stopped = false;

	public ServiceThread() {
		this.thread = new Thread(this, this.getServiceName());
	}

	public abstract String getServiceName();

	public void start() {
		this.thread.start();
	}

	public void shutdown() {
		this.shutdown(false);
	}

	public void stop() {
		this.stop(false);
	}

	public void makeStop() {
		this.stopped = true;
		stlog.info("makestop thread " + this.getServiceName());
	}

	public void stop(final boolean interrupt) {
		this.stopped = true;
		stlog.info("stop thread " + this.getServiceName() + " interrupt "
				+ interrupt);
		synchronized (this) {
			if (!this.hasNotified) {
				this.hasNotified = true;
				this.notify();
			}
		}

		if (interrupt) {
			this.thread.interrupt();
		}
	}

	public void shutdown(final boolean interrupt) {
		this.stopped = true;
		stlog.info("shutdown thread " + this.getServiceName() + " interrupt "
				+ interrupt);
		synchronized (this) {
			if (!this.hasNotified) {
				this.hasNotified = true;
				this.notify();
			}
		}

		try {
			if (interrupt) {
				this.thread.interrupt();
			}

			long beginTime = System.currentTimeMillis();
			this.thread.join(this.getJointime());
			long eclipseTime = System.currentTimeMillis() - beginTime;
			stlog.info("join thread " + this.getServiceName()
					+ " eclipse time(ms) " + eclipseTime + " "
					+ this.getJointime());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void wakeup() {
		synchronized (this) {
			if (!this.hasNotified) {
				this.hasNotified = true;
				this.notify();
			}
		}
	}

	protected void waitForRunning(long interval) {
		synchronized (this) {
			if (this.hasNotified) {
				this.hasNotified = false;
				this.onWaitEnd();
				return;
			}

			try {
				this.wait(interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				this.hasNotified = false;
				this.onWaitEnd();
			}
		}
	}

	protected void onWaitEnd() {
	}

	public boolean isStopped() {
		return stopped;
	}

	public long getJointime() {
		return JOIN_TIME;
	}
}