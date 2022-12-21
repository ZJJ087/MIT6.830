package simpledb.execution;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Type;
import simpledb.storage.BufferPool;
import simpledb.storage.IntField;
import simpledb.storage.Tuple;
import simpledb.storage.TupleDesc;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The delete operator. Delete reads tuples from its child operator and removes
 * them from the table they belong to.
 */
public class Delete extends Operator {

    private static final long serialVersionUID = 1L;
    private TransactionId transactionId;
    private OpIterator child;
    private List<Tuple> tuples = new ArrayList<>();
    private Iterator<Tuple> iterator;

    /**
     * Constructor specifying the transaction that this delete belongs to as
     * well as the child to read from.
     *
     * @param t     The transaction this delete runs in
     * @param child The child operator from which to read tuples for deletion
     */
    public Delete(TransactionId t, OpIterator child) {
        // TODO: some code goes here
        this.transactionId = t;
        this.child = child;
    }

    public TupleDesc getTupleDesc() {
        // TODO: some code goes here
        return new TupleDesc(new Type[]{Type.INT_TYPE});
    }

    public void open() throws DbException, TransactionAbortedException {
        // TODO: some code goes here
        child.open();
        int cnt = 0;
        while(child.hasNext()){
            Tuple next = child.next();
            cnt++;
            try {
                Database.getBufferPool().deleteTuple(transactionId,next);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Tuple t = new Tuple(getTupleDesc());
        t.setField(0, new IntField(cnt));
        tuples.add(t);
        iterator = tuples.iterator();
        super.open();
    }

    public void close() {
        // TODO: some code goes here
        child.close();
        iterator = null;
        super.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // TODO: some code goes here
        child.rewind();
        iterator = tuples.iterator();
    }

    /**
     * Deletes tuples as they are read from the child operator. Deletes are
     * processed via the buffer pool (which can be accessed via the
     * Database.getBufferPool() method.
     *
     * @return A 1-field tuple containing the number of deleted records.
     * @see Database#getBufferPool
     * @see BufferPool#deleteTuple
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // TODO: some code goes here
        if(iterator != null && iterator.hasNext()){
            return iterator.next();
        }
        return null;
    }

    @Override
    public OpIterator[] getChildren() {
        // TODO: some code goes here
        return new OpIterator[]{child};
    }

    @Override
    public void setChildren(OpIterator[] children) {
        // TODO: some code goes here
        this.child = children[0];
    }

}
