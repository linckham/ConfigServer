package com.cmbc.configserver.core.dao.impl;

import com.cmbc.configserver.core.dao.ConfigChangeLogDao;
import com.cmbc.configserver.core.dao.util.JdbcTemplate;
import com.cmbc.configserver.domain.ConfigChangeLog;
import com.cmbc.configserver.utils.ConfigServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/30
 * @Time 19:51
 */
@Service("configChangeLogDao")
public class ConfigChangeLogDaoImpl implements ConfigChangeLogDao {
    private final static String SQL_CHANGE_LOG_INSERT = "insert into config_change_log(path,last_modified_time) values(?,?)";
    private final static String SQL_CHANGE_LOG_UPDATE = "update config_change_log set last_modified_time=? where path=?";
    private final static String SQL_CHANGE_LOG_QUERY = "select * from config_change_log where path=?";
    private final static String SQL_CHANGE_LOG_QUERY_ALL = "select * from config_change_log";

    @Autowired
    private JdbcTemplate jdbcTemplate;
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean add(ConfigChangeLog changeLog) throws Exception {
        try {
            this.jdbcTemplate.update(SQL_CHANGE_LOG_INSERT, new Object[]{
                    changeLog.getPath(),
                    changeLog.getLastModifiedTime()
            });
            return true;
        } catch (Exception ex) {
            ConfigServerLogger.error(new StringBuilder(128).append("insert configuration change log ").append(changeLog).append(" failed. detail is "), ex);
            throw ex;
        }
    }

    @Override
    public boolean update(ConfigChangeLog changeLog) throws Exception {
        try {
            this.jdbcTemplate.update(SQL_CHANGE_LOG_UPDATE, new Object[]{
                    changeLog.getLastModifiedTime(),
                    changeLog.getPath()
            });
            return true;
        } catch (Exception ex) {
            ConfigServerLogger.error(new StringBuilder(128).append("update configuration change log ").append(changeLog).append(" failed. detail is "), ex);
            throw ex;
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public ConfigChangeLog getConfigChangeLog(String path) throws Exception {
        try {
            List<ConfigChangeLog> changeLogs = this.jdbcTemplate.query(SQL_CHANGE_LOG_QUERY, new Object[]{
                    path
            }, new ConfigChangeLogRowMapper());

            if(null == changeLogs || changeLogs.isEmpty()){
                return ConfigChangeLog.EMPTY_MESSAGE;
            }
            return changeLogs.get(0);

        } catch (Exception ex) {
            ConfigServerLogger.error(new StringBuilder(128).append("get configuration change log ").append(path).append(" failed. detail is "), ex);
            throw ex;
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public List<ConfigChangeLog> getAllConfigChangeLogs() throws Exception {
        try {
            return (List<ConfigChangeLog>) this.jdbcTemplate.query(SQL_CHANGE_LOG_QUERY_ALL, new ConfigChangeLogRowMapper());
        } catch (Exception ex) {
            ConfigServerLogger.error(new StringBuilder(128).append("get all config_change_log failed. detail is "), ex);
            throw ex;
        }
    }

    private class ConfigChangeLogRowMapper implements RowMapper {
        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            ConfigChangeLog changeLog;
            try {
                changeLog = new ConfigChangeLog();
                changeLog.setPath(rs.getString("path"));
                changeLog.setLastModifiedTime(rs.getLong("last_modified_time"));
                return changeLog;
            } catch (SQLException ex) {
                ConfigServerLogger.error("error when map row config_change_log: ", ex);
                throw ex;
            }
        }
    }
}