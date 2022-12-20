package simpledb.execution;

import simpledb.common.DbException;
import simpledb.common.Type;
import simpledb.execution.Aggregator.Op;
import simpledb.storage.Tuple;
import simpledb.storage.TupleDesc;
import simpledb.storage.TupleIterator;
import simpledb.transaction.TransactionAbortedException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * The Aggregation operator that computes an aggregate (e.g., sum, avg, max,
 * min). Note that we only support aggregates over a single column, grouped by a
 * single column.
 */
public class Aggregate extends Operator {

    private static final long serialVersionUID = 1L;
    private OpIterator child;
    private int afield;
    private Type afieldType;
    private int gfield;
    private Type gfieldType;
    private Aggregator aggregator;
    private Op op;
    private OpIterator iterator;
    private TupleDesc tupleDesc;


    /**
     * Constructor.
     * <p>
     * Implementation hint: depending on the type of afield, you will want to
     * construct an {@link IntegerAggregator} or {@link StringAggregator} to help
     * you with your implementation of readNext().
     *
     * @param child  The OpIterator that is feeding us tuples.
     * @param afield The column over which we are computing an aggregate.
     * @param gfield The column over which we are grouping the result, or -1 if
     *               there is no grouping
     * @param aop    The aggregation operator to use
     */
    public Aggregate(OpIterator child, int afield, int gfield, Op aop) {
        this.child = child;
        this.afield = afield;
        this.gfield = gfield;
        this.op = op;
        this.afieldType = child.getTupleDesc().getFieldType(afield);
        this.gfieldType = child.getTupleDesc().getFieldType(this.gfield);
        if(afieldType == Type.INT_TYPE){
            this.aggregator = new IntegerAggregator(gfield,gfieldType,afield,aop);
        }else{
            this.aggregator = new StringAggregator(gfield,gfieldType,afield,aop);
        }

    }

    /**
     * @return If this aggregate is accompanied by a groupby, return the groupby
     *         field index in the <b>INPUT</b> tuples. If not, return
     *         {@link Aggregator#NO_GROUPING}
     */
    public int groupField() {
        if(gfield != -1){
            return gfield;
        }
        return -1;
    }

    /**
     * @return If this aggregate is accompanied by a group by, return the name
     *         of the groupby field in the <b>OUTPUT</b> tuples. If not, return
     *         null;
     */
    public String groupFieldName() {
        if(iterator != null && gfield != -1){
            return iterator.getTupleDesc().getFieldName(0);
        }
        return null;
    }

    /**
     * @return the aggregate field
     */
    public int aggregateField() {
        return afield;
    }

    /**
     * @return return the name of the aggregate field in the <b>OUTPUT</b>
     *         tuples
     */
    public String aggregateFieldName() {
        if(iterator != null && gfield == -1){
            return iterator.getTupleDesc().getFieldName(0);
        }else if(iterator != null && gfield != -1){
            return iterator.getTupleDesc().getFieldName(1);
        }
        return null;
    }

    /**
     * @return return the aggregate operator
     */
    public Op aggregateOp() {
        return op;
    }

    public static String nameOfAggregatorOp(Op aop) {
        return aop.toString();
    }

    public void open() throws NoSuchElementException, DbException,
            TransactionAbortedException {
        child.open();
        while(child.hasNext()){
            Tuple tuple = child.next();
            aggregator.mergeTupleIntoGroup(tuple);
        }
        this.iterator = aggregator.iterator();
        iterator.open();
        super.open();
    }

    /**
     * Returns the next tuple. If there is a group by field, then the first
     * field is the field by which we are grouping, and the second field is the
     * result of computing the aggregate. If there is no group by field, then
     * the result tuple should contain one field representing the result of the
     * aggregate. Should return null if there are no more tuples.
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        if (iterator != null){
            if(iterator.hasNext()){
                return iterator.next();
            }
        }
        return null;
    }

    public void rewind() throws DbException, TransactionAbortedException {
        iterator.rewind();
        child.rewind();
    }

    /**
     * Returns the TupleDesc of this Aggregate. If there is no group by field,
     * this will have one field - the aggregate column. If there is a group by
     * field, the first field will be the group by field, and the second will be
     * the aggregate value column.
     * <p>
     * The name of an aggregate column should be informative. For example:
     * "aggName(aop) (child_td.getFieldName(afield))" where aop and afield are
     * given in the constructor, and child_td is the TupleDesc of the child
     * iterator.
     */
    public TupleDesc getTupleDesc() {

        if(gfield == -1){
            StringBuilder builder = new StringBuilder();
            builder.append(aggregateOp().toString()).append("(").append(child.getTupleDesc().getFieldName(afield)).append(")");
            return new TupleDesc(new Type[]{child.getTupleDesc().getFieldType(afield)},new String[]{builder.toString()});
        }
        Type[] types = new Type[2];
        String[] names = new String[2];
        types[1] = child.getTupleDesc().getFieldType(afield);
        names[1] = child.getTupleDesc().getFieldName(afield);
        types[0] = child.getTupleDesc().getFieldType(gfield);
        names[0] = child.getTupleDesc().getFieldName(gfield);
        return new TupleDesc(types,names);

    }

    public void close() {
        iterator.close();
    }

    @Override
    public OpIterator[] getChildren() {
        return new OpIterator[]{child};
    }

    @Override
    public void setChildren(OpIterator[] children) {
        child = children[0];
    }

}
