package simpledb.execution;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Type;
import simpledb.storage.BufferPool;
import simpledb.storage.IntField;
import simpledb.storage.Tuple;
import simpledb.storage.TupleDesc;
import simpledb.transaction.Transaction;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Inserts tuples read from the child operator into the tableId specified in the
 * constructor
 * These operators return the number of affected tuples.
 */
public class Insert extends Operator {

    private static final long serialVersionUID = 1L;
    private TransactionId transactionId;
    private OpIterator child;
    private int tableId;
    private List<Tuple> tuples = new ArrayList<>();
    private Iterator<Tuple> iterator;

    /**
     * Constructor.
     *
     * @param t       The transaction running the insert.
     * @param child   The child operator from which to read tuples to be inserted.
     * @param tableId The table in which to insert tuples.
     * @throws DbException if TupleDesc of child differs from table into which we are to
     *                     insert.
     */
    public Insert(TransactionId t, OpIterator child, int tableId)
            throws DbException {
        // TODO: some code goes here
        if(!child.getTupleDesc().equals(Database.getCatalog().getTupleDesc(tableId))){
            throw new DbException("TupleDesc no match");
        }
        this.transactionId = t;
        this.child = child;
        this.tableId = tableId;
    }

    public TupleDesc getTupleDesc() {
        return new TupleDesc(new Type[]{Type.INT_TYPE});
    }
    /*
    * These operators return the number of affected tuples.
    * This is implemented by returning a single tuple with one integer field, containing the count.*/
    public void open() throws DbException, TransactionAbortedException {
        child.open();
        int count = 0;
        while(child.hasNext()){
            Tuple next = child.next();
            count++;
            try {
                Database.getBufferPool().insertTuple(this.transactionId,this.tableId,next);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Tuple t = new Tuple(getTupleDesc());
        t.setField(0, new IntField(count));
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
     * Inserts tuples read from child into the tableId specified by the
     * constructor. It returns a one field tuple containing the number of
     * inserted records. Inserts should be passed through BufferPool. An
     * instances of BufferPool is available via Database.getBufferPool(). Note
     * that insert DOES NOT need check to see if a particular tuple is a
     * duplicate before inserting it.
     *
     * @return A 1-field tuple containing the number of inserted records, or
     *         null if called more than once.
     * @see Database#getBufferPool
     * @see BufferPool#insertTuple
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
