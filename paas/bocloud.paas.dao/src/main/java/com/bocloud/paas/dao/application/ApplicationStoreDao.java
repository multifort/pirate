package com.bocloud.paas.dao.application;

import java.util.List;
import com.bocloud.paas.entity.ApplicationStore;

public interface ApplicationStoreDao{
	
	public boolean save(ApplicationStore applicationStore) throws Exception;
	
	/**
	 * @return
	 * @throws Exception
	 */
	public List<ApplicationStore> select(String name) throws Exception;
	
	/**
	 * @param template
	 * @return
	 * @throws Exception
	 */
	public ApplicationStore query(String template) throws Exception;
	
	public ApplicationStore query(Long id) throws Exception;
	
	public ApplicationStore detail(String name) throws Exception;
	
	public boolean update(Long id, Long deployNumber) throws Exception;
	
	public boolean update(Long id, String version) throws Exception;
	
	public boolean delete(Long id) throws Exception;
	
	/**
	 * @return
	 * @throws Exception
	 */
	public List<ApplicationStore> selectDeployType(String deployType) throws Exception;
	
}
