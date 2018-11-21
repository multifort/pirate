package com.bocloud.paas.dao.repository.impl;

import com.bocloud.common.model.Param;
import com.bocloud.common.utils.ListTool;
import com.bocloud.common.utils.MapTools;
import com.bocloud.database.core.intf.impl.JdbcGenericDao;
import com.bocloud.database.utils.SQLHelper;
import com.bocloud.paas.dao.repository.ImageDao;
import com.bocloud.paas.entity.Image;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author zjm
 * @date 2017年3月17日
 */
@Repository("imageDao")
public class ImageDaoImpl extends JdbcGenericDao<Image, Long> implements ImageDao {

    @Override
    public Image query(Long id) throws Exception {
        String sql = "SELECT image.*, repository_image_info.namespace, "
                + "repository_image_info.repository_id, repository. NAME AS repository_name, "
                + "cu. NAME AS creator_name, mu. NAME AS mender_name " + "FROM image "
                + "LEFT JOIN repository_image_info on repository_image_info.image_id = image.id "
                + "LEFT JOIN repository on repository_image_info.repository_id = repository.id "
                + "LEFT JOIN `user` cu on cu.id = image.creater_id "
                + "LEFT JOIN `user` mu on  mu.id = image.mender_id " + "WHERE image.is_deleted = 0 and image.id = :id";
        Map<String, Object> params = MapTools.simpleMap("id", id);
        List<Image> list = this.list(Image.class, sql, params);
        if (ListTool.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<Image> list(int page, int rows, List<Param> params, Map<String, String> sorter, String deptId)
            throws Exception {
        String sql = "SELECT image.*, repository_image_info.namespace, "
                + "repository_image_info.repository_id, repository. NAME AS repository_name, "
                + "cu. NAME AS creator_name, mu. NAME AS mender_name " + "FROM image "
                + "LEFT JOIN repository_image_info on repository_image_info.image_id = image.id "
                + "LEFT JOIN repository on repository_image_info.repository_id = repository.id "
                + "LEFT JOIN `user` cu on cu.id = image.creater_id "
                + "LEFT JOIN `user` mu on  mu.id = image.mender_id " + "LEFT JOIN  "
                + "application_image_info on application_image_info.image_id = image.id "
                + "WHERE image.is_deleted = 0 ";
        Map<String, Object> paramMap = SQLHelper.getParam(params);
        if (!StringUtils.isEmpty(deptId)) {
            sql += "and ((image.dept_id is null or image.dept_id in (:deptId)) or repository_image_info.namespace = 'libaray') ";
            paramMap.put("deptId", Arrays.asList(deptId.split(",")));
        }
        sql = SQLHelper.buildRawSql(sql, page, rows, params, sorter, "");
        return this.list(Image.class, sql, paramMap);
    }

    @Override
    public List<Image> list(List<Param> params, Map<String, String> sorter, String deptId, String choice) throws Exception {
        String sql = "SELECT repository.address as repository_address, repository.port as repository_port, repository.type as repository_type, repository.name as repository_name,"
                + " repository_image_info.namespace, image.* FROM image "
                + "LEFT JOIN repository_image_info on repository_image_info.image_id = image.id "
                + "LEFT JOIN repository on repository.id = repository_image_info.repository_id "
                + "WHERE image.is_deleted = 0 ";
        Map<String, Object> paramMap = SQLHelper.getParam(params);
        if (!StringUtils.isEmpty(deptId)) {
            sql += "and ((image.dept_id is null or image.dept_id in (:deptId)) or repository_image_info.namespace = 'libaray') ";
            paramMap.put("deptId", Arrays.asList(deptId.split(",")));
        }
        sql = SQLHelper.buildRawSql(sql, 1, Integer.MAX_VALUE, params, sorter, "");
        List<Image> images = this.list(Image.class, sql, paramMap);
        return images;
    }

    @Override
    public int count(List<Param> params, String deptId, Long repositoryId) throws Exception {
        String sql = "SELECT count(1) FROM image "
                + "LEFT JOIN repository_image_info on repository_image_info.image_id = image.id "
                + "LEFT JOIN repository on repository_image_info.repository_id = repository.id "
                + "LEFT JOIN `user` cu on cu.id = image.creater_id "
                + "LEFT JOIN `user` mu on  mu.id = image.mender_id "
                + "LEFT JOIN  application_image_info on application_image_info.image_id = image.id "
                + "WHERE image.is_deleted = 0 ";
        if (null != repositoryId) {
            sql += "and repository.id = :repositoryId ";
        }
        Map<String, Object> paramMap = SQLHelper.getParam(params);
        if (!StringUtils.isEmpty(deptId)) {
            sql += "and ((image.dept_id is null or image.dept_id in (:deptId)) or repository_image_info.namespace = 'libaray') ";
            paramMap.put("deptId", Arrays.asList(deptId.split(",")));
        }
        sql = SQLHelper.buildRawSql(sql, params, null, "");
       
        if (null != repositoryId) {
            paramMap.put("repositoryId", repositoryId);
        }
        return this.countQuery(sql, paramMap).intValue();
    }

    @Override
    public boolean deleteById(Long id, Long userId) throws Exception {
        String sql = "update image set is_deleted = true , gmt_modify = :gmtModify ,mender_id = :menderId where is_deleted = 0 and id = :id";
        Map<String, Object> params = MapTools.simpleMap("id", id);
        params.put("menderId", userId);
        params.put("gmtModify", new Date());
        return this.execute(sql, params) > 0;
    }

    @Override
    public boolean deleteRepositoryImageInfo(Long id) throws Exception {
        String sql = "delete from repository_image_info where image_id = :id";
        Map<String, Object> params = MapTools.simpleMap("id", id);
        return this.execute(sql, params) >= 0;
    }

    @Override
    public boolean deleteImageAppInfo(Long id) throws Exception {
        String sql = "delete from application_image_info where image_id = :id";
        Map<String, Object> params = MapTools.simpleMap("id", id);
        return this.execute(sql, params) >= 0;
    }

    @Override
    public boolean authorize(Long id, Long userId, Long deptId) throws Exception {
        String sql = "update image set dept_id = :deptId , gmt_modify = :gmtModify ,mender_id = :menderId where is_deleted = 0 and id = :id";
        Map<String, Object> params = MapTools.simpleMap("id", id);
        params.put("deptId", deptId);
        params.put("menderId", userId);
        params.put("gmtModify", new Date());
        return this.execute(sql, params) > 0;
    }

    @Override
    public List<Image> queryAll() throws Exception {
        String sql = "select * from image where is_deleted = 0";
        return this.list(Image.class, sql);
    }

    @Override
    public Image query(String uuid) throws Exception {
        String sql = "SELECT image.*, repository_image_info.namespace, "
                + "repository_image_info.repository_id, repository. NAME AS repository_name, "
                + "cu. NAME AS creator_name, mu. NAME AS mender_name " + "FROM image "
                + "LEFT JOIN repository_image_info on repository_image_info.image_id = image.id "
                + "LEFT JOIN repository on repository_image_info.repository_id = repository.id "
                + "LEFT JOIN `user` cu on cu.id = image.creater_id "
                + "LEFT JOIN `user` mu on  mu.id = image.mender_id " + "WHERE image.is_deleted = 0 and image.uuid = :uuid";
        Map<String, Object> params = MapTools.simpleMap("uuid", uuid);
        List<Image> list = this.list(Image.class, sql, params);
        if (ListTool.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public Image query(Long repositoryId, String namespace, String name, String tag) throws Exception {
        String sql = "SELECT image.*, repository_image_info.namespace, "
                + "repository_image_info.repository_id, repository. NAME AS repository_name, "
                + "cu. NAME AS creator_name, mu. NAME AS mender_name " + "FROM image "
                + "LEFT JOIN repository_image_info on repository_image_info.image_id = image.id "
                + "LEFT JOIN repository on repository_image_info.repository_id = :repositoryId "
                + "LEFT JOIN `user` cu on cu.id = image.creater_id "
                + "LEFT JOIN `user` mu on  mu.id = image.mender_id " + "WHERE image.is_deleted = 0 "
                + "and repository_image_info.namespace = :namespace "
                + "and image.name = :name "
                + "and image.tag = :tag";
        Map<String, Object> params = MapTools.simpleMap("repositoryId", repositoryId);
        params.put("namespace", namespace);
        params.put("name", name);
        params.put("tag", tag);
        List<Image> list = this.list(Image.class, sql, params);
        if (ListTool.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public Image query(String address, String namespace, String name, String tag) throws Exception {
        String sql = "SELECT image.*, repository_image_info.namespace, "
                + "repository_image_info.repository_id, repository. NAME AS repository_name FROM image "
                + "LEFT JOIN repository_image_info on repository_image_info.image_id = image.id "
                + "LEFT JOIN repository on repository_image_info.namespace = :namespace "
                + "WHERE image.is_deleted = 0 "
                + "and repository.address = :address "
                + "and image.name = :name "
                + "and image.tag = :tag";
        Map<String, Object> params = MapTools.simpleMap("address", address);
        params.put("namespace", namespace);
        params.put("name", name);
        params.put("tag", tag);
        List<Image> list = this.list(Image.class, sql, params);
        if (ListTool.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

}
