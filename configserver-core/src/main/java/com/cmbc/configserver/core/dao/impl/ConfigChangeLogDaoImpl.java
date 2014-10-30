package com.cmbc.configserver.core.dao.impl;

import com.cmbc.configserver.core.dao.ConfigChangeLogDao;
import com.cmbc.configserver.core.dao.util.JdbcTemplate;
import com.cmbc.configserver.domain.ConfigChangeLog;
import com.cmbc.configserver.utils.ConfigServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/30
 * @Time 19:51
 */
public class ConfigChangeLogDaoImpl implements ConfigChangeLogDao {
    private static String SQL_CHANGE_LOG_INSERT = "insert into config_change_log(path,md5) values(?,?)";
    private static String SQL_CHANGE_LOG_UPDATE = "update config_change_log set md5=? where path=?";
    private static String SQL_CHANGE_LOG_QUERY = "insert into config_change_log(path,md5) values(?,?)";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public boolean add(ConfigChangeLog changeLog) throws Exception {
        try {
            this.jdbcTemplate.update(SQL_CHANGE_LOG_INSERT, new Object[]{
                    changeLog.getPath(),
                    changeLog.getMd5()
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
                    changeLog.getMd5(),
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
            ConfigChangeLog changeLog = (ConfigChangeLog) this.jdbcTemplate.queryForObject(SQL_CHANGE_LOG_QUERY, new Object[]{
                    path
            }, new ConfigChangeLogRowMapper());
            return changeLog;
        } catch (Exception ex) {
            ConfigServerLogger.error(new StringBuilder(128).append("get configuration change log ").append(path).append(" failed. detail is "), ex);
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
                changeLog.setMd5(rs.getString("md5"));
                return changeLog;
            } catch (SQLException ex) {
                ConfigServerLogger.error("error when map row config_change_log: ", ex);
                throw ex;
            }
        }
    }
}