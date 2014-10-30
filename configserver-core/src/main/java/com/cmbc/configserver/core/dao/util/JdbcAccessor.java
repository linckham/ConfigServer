package com.cmbc.configserver.core.dao.util;
import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.support.SQLExceptionTranslator;
public abstract class JdbcAccessor implements InitializingBean {
	private boolean lazyInit = true;
	/**
	 * Set whether to lazily initialize the SQLExceptionTranslator for this accessor,
	 * on first encounter of a SQLException. Default is "true"; can be switched to
	 * "false" for initialization on startup.
	 * <p>Early initialization just applies if <code>afterPropertiesSet()</code> is called.
	 * @see #afterPropertiesSet()
	 */
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}
	/**
	 * Return whether to lazily initialize the SQLExceptionTranslator for this accessor.
	 */
	public boolean isLazyInit() {
		return this.lazyInit;
	}
	/**
	 * Eagerly initialize the exception translator, if demanded,
	 * creating a default one for the specified DataSource if none set.
	 */
	public void afterPropertiesSet(DataSource dataSource) {
		if (dataSource == null) {
			throw new IllegalArgumentException("Property 'dataSource' is required");
		}
		if (!isLazyInit()) {
			getExceptionTranslator(dataSource);
		}
	}
	@Override
	public void afterPropertiesSet() throws Exception {
	}
	public abstract SQLExceptionTranslator getExceptionTranslator(DataSource dataSource);
}