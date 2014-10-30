package com.cmbc.configserver.core.dao.impl;

import com.cmbc.configserver.core.dao.ConfigClientMappingDao;
import com.cmbc.configserver.core.dao.util.JdbcTemplate;
import com.cmbc.configserver.domain.ConfigClientMapping;
import com.cmbc.configserver.utils.ConfigServerLogger;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date 2014/10/30
 * @Time 18:35
 */
public class ConfigClientMappingDaoImpl implements ConfigClientMappingDao {
    private static String SQL_MAPPING_INSERT = "insert into config_client_mapping(config_id,client_id) values(?,?)";
    private static String SQL_MAPPING_DELETE_BY_CONFIG_ID = "delete from config_client_mapping where config_id=?";
    private static String SQL_MAPPING_DELETE_BY_CLIENT_ID = "delete from config_client_mapping where client_id=?";
    private static String SQL_MAPPING_QUERY_BY_CLIENT_ID = "select * from config_client_mapping where client_id=?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public boolean save(ConfigClientMapping ccMapping) throws Exception {
        try {
            this.jdbcTemplate.update(SQL_MAPPING_INSERT, new Object[]{
                    ccMapping.getConfigId(),
                    ccMapping.getClientId()
            });
            return true;
        } catch (Exception ex) {
            ConfigServerLogger.error(new StringBuilder(128).append("insert config_client_mapping ").append(ccMapping).append("failed. Details is "), ex);
            throw ex;
        }
    }

    @Override
    public boolean delete(ConfigClientMapping ccMapping) throws Exception {
        boolean deleteByConfigId = ccMapping.getConfigId() > 0;
        try {
            if (deleteByConfigId) {
                this.jdbcTemplate.update(SQL_MAPPING_DELETE_BY_CONFIG_ID, new Object[]{
                        ccMapping.getConfigId()
                });
            } else {
                this.jdbcTemplate.update(SQL_MAPPING_DELETE_BY_CLIENT_ID, new Object[]{
                        ccMapping.getClientId()
                });
            }
            return true;
        } catch (Exception ex) {
            ConfigServerLogger.error(new StringBuilder(128)
                    .append("delete config_client_mapping by ")
                    .append(deleteByConfigId ? "config_id" : "client_id")
                    .append(deleteByConfigId ? ccMapping.getConfigId() : ccMapping.getClientId())
                    .append("failed. Details is "), ex);
            throw ex;
        }
    }

    /**
     * get the specified client id's all configuration id list
     *
     * @param clientId the specified client id
     * @throws Exception
     */
    @Override
    public List<ConfigClientMapping> getMappingList(String clientId) throws Exception {
        return null;
    }

    private class MappingRower implements RowMapper {

        @Override
        public ConfigClientMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
            ConfigClientMapping mapping;
            try {
                mapping = new ConfigClientMapping();
                mapping.setClientId(rs.getString("client_id"));
                mapping.setConfigId(rs.getInt("config_id"));
                return mapping;
            } catch (SQLException ex) {
                ConfigServerLogger.error("error when map row config_client_mapping: ", ex);
                throw ex;
            }
        }
    }
}
