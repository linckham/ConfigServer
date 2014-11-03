package com.cmbc.configserver.core.dao.impl;

import com.cmbc.configserver.core.dao.ConfigDetailsDao;
import com.cmbc.configserver.core.dao.util.JdbcTemplate;
import com.cmbc.configserver.domain.Category;
import com.cmbc.configserver.domain.Configuration;
import com.cmbc.configserver.utils.ConfigServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.*;
import java.util.Collections;
import java.util.List;

/**
 * Created by tongchuan.lin<linckham@gmail.com><br/>
 *
 * @Date: 2014/10/30
 * @Time: 14:20
 */
public class ConfigDetailsDaoImpl implements ConfigDetailsDao {
    private final static String SQL_CONFIG_INSERT = "insert into config_details(category_id,content,client_id) values(?,?,?)";
    private static String SQL_CONFIG_UPDATE = "update config_details set content=?,client_id=? where config_id=?";
    private static String SQL_CONFIG_DELETE = "delete from config_details where client_id=? and category_id=?";
    private static String SQL_CONFIG_QUERY_BY_CATEGORY_ID = "select * from config_details where category_id=?";
    private static String SQL_CONFIG_QUERY_BY_CLIENT_ID = "select * from config_details where client_id=?";
    private static String SQL_CONFIG_DELETE_BY_CLIENT_ID = "delete  from config_details where client_id=?";
    private static String SQL_CONFIG_CATEGORY_ID_BY_CLIENT_ID = "select distinct category_id from config_details where client_id=?";


    @Autowired
    private JdbcTemplate jdbcTemplate;
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Configuration save(final Configuration config) throws Exception {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            this.jdbcTemplate.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement preState =  con.prepareStatement(SQL_CONFIG_INSERT, Statement.RETURN_GENERATED_KEYS);
                    preState.setInt(1,config.getCategoryId());
                    preState.setString(2,config.getContent());
                    preState.setString(3,config.getClientId());
                    return preState;
                }
            },keyHolder);
            config.setId(keyHolder.getKey().intValue());
            return config;
        } catch (Exception ex) {
            ConfigServerLogger.error(new StringBuilder(128).append("insert configuration ").append(config.toString()).append("failed. detail is "), ex);
            throw ex;
        }
    }

    @Override
    public boolean update(Configuration config) throws Exception {
        try {
            this.jdbcTemplate.update(SQL_CONFIG_UPDATE, new Object[]{
                    config.getContent(),
                    config.getClientId(),
                    config.getId()
            });
            return true;
        } catch (Exception ex) {
            ConfigServerLogger.error(new StringBuilder(128).append("update configuration ").append(config.toString()).append("failed. detail is "), ex);
            throw ex;
        }
    }

    @Override
    public boolean delete(Configuration config) throws Exception {
        try {
            this.jdbcTemplate.update(SQL_CONFIG_DELETE, new Object[]{
                    config.getClientId(),
                    config.getCategoryId()
            });
            return true;
        } catch (Exception ex) {
            ConfigServerLogger.error(new StringBuilder(128).append("delete configuration ").append(config.toString()).append(" of the client ").append(config.getClientId()).append(" failed. detail is "), ex);
            throw ex;
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<Configuration> getConfigurationList(Category category) throws Exception {
        try {
            return (List<Configuration>) this.jdbcTemplate.query(SQL_CONFIG_QUERY_BY_CATEGORY_ID, new Object[]{
                    category.getId()
            }, new ConfigurationRowMapper(category));
        } catch (Exception ex) {
            ConfigServerLogger.error(new StringBuilder(128).append("get configuration of the category ").append(category).append(" failed. detail is "), ex);
            throw ex;
        }
    }

    @Override
    public List<Configuration> getConfigurationListByClientId(String clientId) throws Exception {
        try {
            return (List<Configuration>) this.jdbcTemplate.query(SQL_CONFIG_QUERY_BY_CLIENT_ID, new Object[]{
                    clientId
            }, new ConfigurationRowMapper(null));
        } catch (Exception ex) {
            ConfigServerLogger.error(new StringBuilder(128).append("get configuration of the client ").append(clientId).append(" failed. detail is "), ex);
            throw ex;
        }
    }

    @Override
    public boolean deleteConfigurationByClientId(String clientId) throws Exception {
        try{
            this.jdbcTemplate.update(SQL_CONFIG_DELETE_BY_CLIENT_ID,new Object[]{clientId});
            return true;
        }
        catch(Exception ex){
            ConfigServerLogger.error(new StringBuilder(128).append("delete configuration items of the client ").append(clientId).append(" failed. detail is "), ex);
            throw ex;
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public List<Integer> getCategoryIdsByClientId(String clientId) throws Exception {
        try{
            List<Integer> categoryIds = (List<Integer>) this.jdbcTemplate.queryForList(SQL_CONFIG_CATEGORY_ID_BY_CLIENT_ID,new Object[]{
                    clientId
            },Integer.class);

            if(null == categoryIds || categoryIds.isEmpty()){
                return Collections.EMPTY_LIST;
            }

            return categoryIds;
        }
        catch (Exception ex){
            ConfigServerLogger.error(new StringBuilder(128).append("get category id of the client ").append(clientId).append(" failed. detail is "), ex);
            throw ex;
        }
    }

    private class ConfigurationRowMapper implements RowMapper {
        private Category category;

        public ConfigurationRowMapper(Category category) {
            this.category = category;
        }

        @Override
        public Configuration mapRow(ResultSet rs, int rowNum) throws SQLException {
            Configuration config;
            try {
                config = new Configuration();
                //
                if (null != category) {
                    config.setCell(category.getCell());
                    config.setResource(category.getResource());
                    config.setType(category.getType());
                }
                config.setId(rs.getInt("config_id"));
                config.setCategoryId(rs.getInt("category_id"));
                config.setClientId(rs.getString("client_id"));
                config.setContent(rs.getString("content"));

                return config;
            } catch (SQLException ex) {
                ConfigServerLogger.error("error when map row config_details: ", ex);
                throw ex;
            }
        }
    }
}