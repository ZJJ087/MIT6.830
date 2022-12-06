package simpledb.storage;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Permissions;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author zhangjiajun
 * @date 2022/12/6 15:43
 * @description
 */
public class HeapFileIterator implements DbFileIterator{
    TransactionId tid;
    Permissions permissions;
    BufferPool bufferPool = Database.getBufferPool();
    Iterator<Tuple> iterator;  //这个iterator是每一页的迭代器
    int num = 0;

    @Override
    public void open() throws DbException, TransactionAbortedException {

    }

    @Override
    public boolean hasNext() throws DbException, TransactionAbortedException {
        return false;
    }

    @Override
    public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
        return null;
    }

    @Override
    public void rewind() throws DbException, TransactionAbortedException {

    }

    @Override
    public void close() {

    }
}
