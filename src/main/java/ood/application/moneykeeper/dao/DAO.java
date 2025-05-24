package ood.application.moneykeeper.dao;

import java.util.List;

public interface DAO<T, K> {

    public List<T> getAll();

    public T get(K k);// key

    public boolean save(T t);

    public boolean update(T t);

    public boolean delete(T t);
}
