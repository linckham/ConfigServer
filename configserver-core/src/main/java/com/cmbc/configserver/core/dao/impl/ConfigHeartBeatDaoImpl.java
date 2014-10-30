package com.cmbc.configserver.core.dao.impl;

import com.cmbc.configserver.core.dao.ConfigHeartBeatDao;
import com.cmbc.configserver.core.dao.util.JdbcTemplate;
import com.cmbc.configserver.domain.ConfigHeartBeat;
import com.cmbc.configserver.utils.ConfigServerLogger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/30
 * @Time 20:38
 */
public class ConfigHeartBeatDaoImpl implements ConfigHeartBeatDao {
    private static String SQL_HEARTBEAT_INSERT = "insert into config_heart_beat(client_id,last_modified_time) values(?,?)";
    private static String SQL_HEARTBEAT_UPDATE = "update config_heart_beat set last_modified_time=? where client_id=?";
    private static String SQL_HEARTBEAT_DELETE = "delete from config_heart_beat where client_id=?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public boolean save(ConfigHeartBeat heartBeat) throws Exception {
        try {
            this.jdbcTemplate.update(SQL_HEARTBEAT_INSERT, new Object[]{
                    heartBeat.getClientId(),
                    heartBeat.getLastModifiedTime()
            });
            return true;
        } catch (Exception ex) {
            ConfigServerLogger.error(new StringBuilder(128).append("insert configuration heart beat ").append(heartBeat).append(" failed. detail is "), ex);
            throw ex;
        }
    }

    @Override
    public boolean update(ConfigHeartBeat heartBeat) throws Exception {
        try {
            this.jdbcTemplate.update(SQL_HEARTBEAT_UPDATE, new Object[]{
                    heartBeat.getLastModifiedTime(),
                    heartBeat.getClientId()
            });
            return true;
        } catch (Exception ex) {
            ConfigServerLogger.error(new StringBuilder(128).append("update configuration heart beat ").append(heartBeat).append(" failed. detail is "), ex);
            throw ex;
        }
    }

    @Override
    public boolean delete(ConfigHeartBeat heartBeat) throws Exception {
        try {
            this.jdbcTemplate.update(SQL_HEARTBEAT_DELETE, new Object[]{
                    heartBeat.getLastModifiedTime(),
                    heartBeat.getClientId()
            });
            return true;
        } catch (Exception ex) {
            ConfigServerLogger.error(new StringBuilder(128).append("delete configuration heart beat ").append(heartBeat).append(" failed. detail is "), ex);
            throw ex;
        }
    }
}