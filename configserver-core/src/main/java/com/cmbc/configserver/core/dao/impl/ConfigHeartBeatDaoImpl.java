package com.cmbc.configserver.core.dao.impl;

import com.cmbc.configserver.core.dao.ConfigHeartBeatDao;
import com.cmbc.configserver.core.dao.util.JdbcTemplate;
import com.cmbc.configserver.domain.ConfigHeartBeat;
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
 * @Time 20:38
 */
@Service("configHeartBeatDao")
public class ConfigHeartBeatDaoImpl implements ConfigHeartBeatDao {
    private final static String SQL_HEARTBEAT_INSERT = "insert into config_heart_beat(client_id,last_modified_time) values(?,?)";
    private final static String SQL_HEARTBEAT_UPDATE = "update config_heart_beat set last_modified_time=? where client_id=?";
    private final static String SQL_HEARTBEAT_DELETE = "delete from config_heart_beat where client_id=?";
    private final static String SQL_HEARTBEAT_GET = "select * from config_heart_beat where client_id=? for update";
    private final static String SQL_HEARTBEAT_GET_TIMEOUT = " select * from config_heart_beat where last_modified_time < ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

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
    public boolean delete(String  clientId) throws Exception {
        try {
            this.jdbcTemplate.update(SQL_HEARTBEAT_DELETE, new Object[]{clientId});
            return true;
        } catch (Exception ex) {
            ConfigServerLogger.error(new StringBuilder(128).append("delete configuration heart beat ").append(clientId).append(" failed. detail is "), ex);
            throw ex;
        }
    }

	@Override
    @SuppressWarnings({"unchecked"})
	public ConfigHeartBeat get(String clientId) throws Exception {
		try {
			List<ConfigHeartBeat> heartBeats = (List<ConfigHeartBeat>)this.jdbcTemplate.query(SQL_HEARTBEAT_GET, new Object[] { clientId },
					new HeartBeatMapper());
            if(null == heartBeats || heartBeats.isEmpty()){
                return null;
            }
            return heartBeats.get(0);

		} catch (Exception ex) {
			ConfigServerLogger.error("get client heartbeat error: " + clientId, ex);
			throw ex;
		}
	}

	private class HeartBeatMapper implements RowMapper {
        @Override
        public ConfigHeartBeat mapRow(ResultSet rs, int rowNum) throws SQLException {
        	ConfigHeartBeat heartbeat;
            try {
            	heartbeat = new ConfigHeartBeat();
                heartbeat.setClientId(rs.getString("client_id"));
                heartbeat.setLastModifiedTime(rs.getLong("last_modified_time"));
                return heartbeat;
            } catch (SQLException ex) {
                ConfigServerLogger.error("error when map row config_category: ", ex);
                throw ex;
            }
        }
    }

	@Override
	@SuppressWarnings({"unchecked"})
	public List<ConfigHeartBeat> getTimeout() throws Exception {
        try {
            return this.jdbcTemplate.query(SQL_HEARTBEAT_GET_TIMEOUT,
                    new Object[]{System.currentTimeMillis() - ConfigHeartBeat.DB_TIMEOUT}, new HeartBeatMapper());
        } catch (Exception ex) {
            ConfigServerLogger.error(new StringBuilder(128).append("get timeout list error "), ex);
            throw ex;
        }
    }
}