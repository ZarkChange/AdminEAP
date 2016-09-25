package com.cnpc.framework.base.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.type.Type;

import com.cnpc.framework.base.pojo.PageInfo;

/**
 * @author bin
 *
 */
public interface BaseDao {

    /**
     * 保存对象
     * 
     * @param obj
     *            所要保存的对象
     * @return 唯一主键
     */
    public <T> Serializable save(T obj);

    /**
     * 删除对象
     * 
     * @param obj
     *            所要删除的对象
     */
    public <T> void delete(T obj);

    /**
     * 修改对象
     * 
     * @param obj
     *            所要修改的对象
     */
    public <T> void update(T obj);

    /**
     * 保存或修改
     * 
     * @param obj
     *            所要修改的对象
     */
    public Object saveOrUpdate(Object obj);

    /**
     * 批量保存
     */
    public <T> void batchSave(List<T> entityList);

    /**
     * 批量更新
     */
    public <T> void batchUpdate(List<T> entityList);

    /**
     * 批量删除
     */
    public <T> void batchDelete(List<T> entityList);

    /**
     * 批量保存或修改
     */
    public <T> void batchSaveOrUpdate(List<T> entityList);

    /**
     * 查询此对象所有数据
     * 
     * @param clazz
     * @return
     */
    public <T> List<T> list(Class<T> clazz);

    /**
     * 根据主键获取对象
     * 
     * @param clazz
     *            所要获取对象的类
     * @param id
     *            主键
     * @return
     */
    public <T> T get(Class<T> clazz, Serializable id);

    /**
     * hql语句查询单个实体对象
     * 
     * @param hql
     *            查询语句
     * @return 实体对象
     */
    public <T> T get(String hql);

    /**
     * hql语句带条件查询单个实体对象
     * 
     * @param hql
     *            查询语句
     * @param params
     *            条件参数
     * @return 实体对象
     */
    public <T> T get(String hql, Map<String, Object> params);

    /**
     * hql语句查询实体集合
     * 
     * @param hql
     *            查询
     * @return
     */
    public <T> List<T> find(String hql);

    public List<Map> findMap(String hql);

    /**
     * hql语句带条件查询实体集合
     * 
     * @param hql
     *            查询语句
     * @param params
     *            条件参数
     * @return
     */
    public <T> List<T> find(String hql, Map<String, Object> params);

    /**
     * hql语句分页查询实体集合
     * 
     * @param hql
     *            查询语句
     * @param page
     *            当前页号
     * @param rows
     *            行数
     * @return
     */
    public <T> List<T> find(String hql, int page, int rows);

    /**
     * hql语句带条件分页查询实体集合
     * 
     * @param hql
     *            查询语句
     * @param params
     *            条件参数
     * @param page
     *            当前页号
     * @param rows
     *            行数
     * @return
     */
    public <T> List<T> find(String hql, Map<String, Object> params, int page, int rows);

    /**
     * hql语句查询记录数
     * 
     * @param hql
     *            查询语句
     * @return
     */
    public Long count(String hql);

    /**
     * hql语句带条件查询记录数
     * 
     * @param hql
     *            查询语句
     * @param params
     *            条件参数
     * @return
     */
    public Long count(String hql, Map<String, Object> params);

    /**
     * 执行hql语句（可带事务）
     * 
     * @param hql
     *            查询语句
     * @return
     */
    public int executeHql(String hql);

    /**
     * sql查询获取实体对象
     * 
     * @param sql
     *            查询语句
     * @return
     */
    public <T> T getBySql(String sql);

    /**
     * sql带条件查询获取实体对象
     * 
     * @param sql
     *            查询语句
     * @param params
     *            条件参数
     * @return
     */
    public <T> T getBySql(String sql, Map<String, Object> params);

    /**
     * sql查询实体结果集
     * 
     * @param sql
     *            查询语句
     * @param clazz
     *            类对象
     * @return
     */
    public <T> List<T> findBySql(String sql, Class<T> clazz);

    /**
     * sql带条件查询实体结果集
     * 
     * @param sql
     *            查询语句
     * @param params
     *            条件参数
     * @param clazz
     *            类对象
     * @return
     */
    public <T> List<T> findBySql(String sql, Map<String, Object> params, Class<T> clazz);

    /**
     * sql分页查询结果集
     * 
     * @param sql
     *            查询语句
     * @param page
     *            当前页
     * @param rows
     *            行数
     * @param clazz
     *            类对象
     * @return
     */
    public <T> List<T> findBySql(String sql, int page, int rows, Class<T> clazz);

    /**
     * sql分页查询结果集
     * 
     * @param sql
     *            查询语句
     * @param params
     *            条件参数
     * @param page
     *            当前页
     * @param rows
     *            行数
     * @param clazz
     *            类对象
     * @return
     */
    public <T> List<T> findBySql(String sql, Map<String, Object> params, int page, int rows, Class<T> clazz);

    /**
     * sql查询Map结果集
     * 
     * @param sql
     *            查询语句
     * @return
     */
    public List<Map<String, Object>> findMapBySql(String sql);

    /**
     * sql带条件查询Map结果集
     * 
     * @param sql
     *            查询语句
     * @param params
     *            条件参数
     * @return
     */
    public List<Map<String, Object>> findMapBySql(String sql, Map<String, Object> params);

    /**
     * sql分页查询Map结果集
     * 
     * @param sql
     *            查询语句
     * @param page
     *            当前页号
     * @param rows
     *            行数
     * @return
     */
    public List<Map<String, Object>> findMapBySql(String sql, int page, int rows);

    /**
     * sql带条件分页查询Map结果集
     * 
     * @param sql
     *            查询语句
     * @param params
     *            条件参数
     * @param page
     *            当前页
     * @param rows
     *            行数
     * @return
     */
    public List<Map<String, Object>> findMapBySql(String sql, Map<String, Object> params, int page, int rows);

    /**
     * sql查询记录数
     * 
     * @param sql
     *            查询语句
     * @return
     */
    public Long countBySql(String sql);

    /**
     * sql带条件查询记录数
     * 
     * @param sql
     *            查询语句
     * @param params
     *            条件参数
     * @return
     */
    public Long countBySql(String sql, Map<String, Object> params);

    /**
     * 执行sql语句（带事务）
     * 
     * @param sql
     *            执行语句
     * @return
     */
    public int executeSql(String sql);

    public <T> List<T> getListByCriteria(DetachedCriteria criteria, PageInfo page);

    public List<?> getListByCriteria(DetachedCriteria criteria, Integer startPage, Integer pageSize);

    public int getCountByCriteria(DetachedCriteria criteria);

    public List findByExample(Object example);

    public <T> List<T> findByCriteria(DetachedCriteria criteria);

    public List findMapBySql(String sql, int firstResult, int maxResult, Object[] params, Type[] types, Class clazz);

    public Long countBySql(String sql, Object[] params, Type[] types);

    public List findMapBySql(String sql, Object[] params, Type[] types, Class clazz);

    List findByExample(Object example, String condition, boolean enableLike);

}
