package simpledb.execution;

import simpledb.common.Type;
import simpledb.storage.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Knows how to compute some aggregate over a set of IntFields.
 * IntegerAggregator有count、sum、max、min、avg这5个操作
 */
public class IntegerAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;
    private static final String NO_GROUPING_KEY = "NO_GROUPING_KEY";
    private int groupByFieldIndex;
    private Type groupByFieldType;
    private int aggregateFieldIndex;
    private Op op;
    private GroupByHandler groupByHandler;

    private abstract class GroupByHandler{
        ConcurrentHashMap<String,Integer> groupByResult;
        abstract void handle(String key, Field field);
        private GroupByHandler(){
            groupByResult = new ConcurrentHashMap<>();
        }
        public Map<String,Integer> getGroupByResult(){
            return groupByResult;
        }
    }

    /**
     * Aggregate constructor
     * gb- group by | a-aggregate
     *
     * @param gbfield     the 0-based index of the group-by field in the tuple, or
     *                    NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null
     *                    if there is no grouping
     * @param afield      the 0-based index of the aggregate field in the tuple
     * @param what        the aggregation operator
     */

    public IntegerAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        this.groupByFieldIndex = gbfield;
        this.groupByFieldType = gbfieldtype;
        this.aggregateFieldIndex = afield;
        this.op = what;
        switch (what){
            case AVG:
                groupByHandler = new AvgHandler();
                break;
            case MAX:
                groupByHandler = new MaxHandler();
                break;
            case MIN:
                groupByHandler = new MinHandler();
                break;
            case COUNT:
                groupByHandler = new CountHandler();
                break;
            case SUM:
                groupByHandler = new SumHandler();
                break;
        }
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the
     * constructor
     *
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        if(groupByFieldType != null && (!tup.getField(groupByFieldIndex).getType().equals(groupByFieldType))){
            throw new IllegalStateException("Illegal Field Type");
        }
        String key;
        if(groupByFieldIndex == NO_GROUPING){
            key = NO_GROUPING_KEY;
        }else{
            key = tup.getField(groupByFieldIndex).toString();
        }
        groupByHandler.handle(key,tup.getField(aggregateFieldIndex));
    }

    /**
     * Create a OpIterator over group aggregate results.
     *
     * @return a OpIterator whose tuples are the pair (groupVal, aggregateVal)
     *         if using group, or a single (aggregateVal) if no grouping. The
     *         aggregateVal is determined by the type of aggregate specified in
     *         the constructor.
     */
    public OpIterator iterator() {
        ConcurrentHashMap<String, Integer> groupByResult = groupByHandler.groupByResult;
        Type[] types;
        String[] names;
        TupleDesc tupleDesc;
        List<Tuple> tuples = new ArrayList<>();
        if(groupByFieldIndex == NO_GROUPING){ //(aggregateVal)
            types = new Type[]{Type.INT_TYPE};
            names = new String[]{"aggregateVal"};
            tupleDesc = new TupleDesc(types, names);
            for(Map.Entry<String,Integer> entry: groupByResult.entrySet()){
                Integer value = entry.getValue();
                Tuple tuple = new Tuple(tupleDesc);
                tuple.setField(0,new IntField(value));
                tuples.add(tuple);
            }
        }else{ //(aggregateVal,groupVal)
            names = new String[]{"groupByVal","aggregateVal"};
            if(groupByFieldIndex != NO_GROUPING && groupByFieldType == Type.INT_TYPE){
                types = new Type[]{Type.INT_TYPE,Type.INT_TYPE};
            }else if(groupByFieldIndex != NO_GROUPING && groupByFieldType == Type.STRING_TYPE){
                types = new Type[]{Type.STRING_TYPE,Type.INT_TYPE};
            }else {
                types = new Type[]{};
            }
            tupleDesc = new TupleDesc(types,names);
            for(Map.Entry<String,Integer> entry: groupByResult.entrySet()){
                Integer aggregateVal = entry.getValue();
                String groupVal = entry.getKey();
                if(groupByFieldType == Type.INT_TYPE){
                    int group = Integer.parseInt(groupVal);
                    Tuple tuple = new Tuple(tupleDesc);
                    tuple.setField(1,new IntField(aggregateVal));
                    tuple.setField(0,new IntField(group));
                    tuples.add(tuple);
                }else if(groupByFieldType == Type.STRING_TYPE){
                    Tuple tuple = new Tuple(tupleDesc);
                    tuple.setField(1,new IntField(aggregateVal));
                    tuple.setField(0,new StringField(groupVal,30));
                    tuples.add(tuple);
                }
            }
        }
        return new TupleIterator(tupleDesc,tuples);
    }

    private class SumHandler extends GroupByHandler{
        @Override
        void handle(String key, Field field) {
            if (groupByResult.containsKey(key)){
                int i = Integer.parseInt(field.toString());
                groupByResult.put(key,groupByResult.get(key) + i);
            }else{
                groupByResult.put(key,Integer.parseInt(field.toString()));
            }
        }
    }
    private class MaxHandler extends GroupByHandler{
        @Override
        void handle(String key, Field field) {
            if (groupByResult.containsKey(key)){
                int val = Math.max(groupByResult.get(key),Integer.parseInt(field.toString()));
                groupByResult.put(key,val);
            }else{
                groupByResult.put(key,Integer.parseInt(field.toString()));
            }
        }
    }

    private class MinHandler extends GroupByHandler{
        @Override
        void handle(String key, Field field) {
            if (groupByResult.containsKey(key)){
                int val = Math.min(groupByResult.get(key),Integer.parseInt(field.toString()));
                groupByResult.put(key,val);
            }else{
                groupByResult.put(key,Integer.parseInt(field.toString()));
            }
        }
    }

    private class CountHandler extends GroupByHandler{
        @Override
        void handle(String key, Field field) {
            if(groupByResult.containsKey(key)){
                groupByResult.merge(key,1,Integer::sum);
            }else{
                groupByResult.put(key,1);
            }
        }
    }

    private class AvgHandler extends GroupByHandler{
        ConcurrentHashMap<String,Integer> count;
        ConcurrentHashMap<String,Integer> sum;
        private AvgHandler(){
            this.sum = new ConcurrentHashMap<>();
            this.count = new ConcurrentHashMap<>();
        }
        @Override
        void handle(String key, Field field) {
            if(groupByResult.containsKey(key)){
                count.put(key,count.get(key)+1);
                sum.put(key,sum.get(key) + Integer.parseInt(field.toString()));
            }else{
                count.put(key,1);
                sum.put(key,Integer.parseInt(field.toString()));
            }
            groupByResult.put(key,sum.get(key)/count.get(key));
        }
    }
}
