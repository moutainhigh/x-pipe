package com.ctrip.xpipe.redis.console.dao;

import com.ctrip.xpipe.redis.console.exception.ServerException;
import com.ctrip.xpipe.redis.console.model.ConfigTbl;
import com.ctrip.xpipe.redis.console.model.ConfigTblDao;
import com.ctrip.xpipe.redis.console.model.ConfigTblEntity;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.springframework.stereotype.Repository;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.ContainerLoader;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * @author wenchao.meng
 *         <p>
 *         Jun 15, 2017
 */
@Repository
public class ConfigDao extends AbstractXpipeConsoleDAO{

    private ConfigTblDao configTblDao;

    @PostConstruct
    private void postConstruct() {
        try {
            configTblDao = ContainerLoader.getDefaultContainer().lookup(ConfigTblDao.class);
        } catch (ComponentLookupException e) {
            throw new ServerException("Cannot construct dao.", e);
        }
    }

    public String getKey(String key) throws DalException {

        ConfigTbl byKey = configTblDao.findByKey(key, ConfigTblEntity.READSET_VALUE);
        return byKey.getValue();
    }

    protected ConfigTbl findByKey(long id) throws DalException {

        return configTblDao.findByPK(id, ConfigTblEntity.READSET_FULL);
    }

    public synchronized void setKey(String key, String value) throws DalException {

        set(key, value, null);
    }

    public synchronized void setKeyAndUntil(String key, String val, Date until) throws DalException {
        set(key, val, until);

    }

    public ConfigTbl getByKey(String key) throws DalException {
        return configTblDao.findByKey(key, ConfigTblEntity.READSET_FULL);
    }

    private void set(String key, String value, Date until) throws DalException {
        boolean insert = false;

        try{
            getKey(key);
        }catch (DalNotFoundException e){
            logger.info("[setKey][not exist, create]{}", e.getMessage());
            insert = true;
        }

        ConfigTbl configTbl = new ConfigTbl();
        configTbl.setKey(key);
        configTbl.setValue(value);

        if(until != null) {
            configTbl.setUntil(until);
        }
        if(!insert) {
            configTblDao.updateValAndUntilByKey(configTbl, ConfigTblEntity.UPDATESET_FULL);
        }else{
            configTblDao.insert(new ConfigTbl().setKey(key)
                    .setValue(value).setDesc("insert automatically").setUntil(until));
        }
    }
}
