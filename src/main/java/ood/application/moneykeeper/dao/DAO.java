package ood.application.moneykeeper.dao;

import java.sql.SQLException;
import java.util.List;

public interface DAO<T, K> {

    public List<T> getAll() throws SQLException;

    public T get(K k) throws SQLException;// key

    public boolean save(T t) throws SQLException;

    public boolean update(T t) throws SQLException;

    public boolean delete(T t) throws SQLException;
}
