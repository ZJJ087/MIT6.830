package simpledb.storage;

import simpledb.common.Type;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc implements Serializable {

    public final TDItem[] tdItems;


    /**
     * A help class to facilitate organizing the information of each field
     */
    public static class TDItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The type of the field
         */
        public final Type fieldType;

        /**
         * The name of the field
         */
        public final String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }
    }

    /**
     * @return An iterator which iterates over all the field TDItems
     *         that are included in this TupleDesc
     */
    public Iterator<TDItem> iterator() {
        if(tdItems == null) {
            return null;
        }
        return null;
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     *
     * @param typeAr  array specifying the number of and types of fields in this
     *                TupleDesc. It must contain at least one entry.
     * @param fieldAr array specifying the names of the fields. Note that names may
     *                be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
        int len = typeAr.length;
        tdItems = new TDItem[len];
        for(int i = 0; i < len; i++){
            TDItem tdItem = new TDItem(typeAr[i], fieldAr[i]);
            tdItems[i] = tdItem;
        }
    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     *
     * @param typeAr array specifying the number of and types of fields in this
     *               TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
        // TODO: some code goes here
        int len = typeAr.length;
        tdItems = new TDItem[len];
        for(int i = 0; i < len ; i++){
            TDItem tdItem = new TDItem(typeAr[i], "");
            tdItems[i] = tdItem;
        }
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        return tdItems.length;
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     *
     * @param i index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        if(i < 0 || i > tdItems.length-1){
            throw new NoSuchElementException("i is not a valid index");
        }
        return tdItems[i].fieldName;
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     *
     * @param i The index of the field to get the type of. It must be a valid
     *          index.
     * @return the type of the ith field
     * @throws NoSuchElementException if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        if(i < 0 || i > tdItems.length-1){
            throw new NoSuchElementException("i is not a legal index");
        }
        return tdItems[i].fieldType;
    }

    /**
     * Find the index of the field with a given name.
     *
     * @param name name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException if no field with a matching name is found.
     */
    public int indexForFieldName(String name) throws NoSuchElementException {
        if(name == null){
            throw new NoSuchElementException("name is null");
        }
        for(int i = 0; i < tdItems.length; i++){
            if(name.equals(tdItems[i].fieldName)){
                return i;
            }
        }
        throw  new NoSuchElementException("No such Element");
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     *         Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        // TODO: some code goes here
        int size = 0;
        for(TDItem item: tdItems){
            int len = item.fieldType.getLen();
            size += len;
        }
        return size;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     *
     * @param td1 The TupleDesc with the first fields of the new TupleDesc
     * @param td2 The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
        int l1 = td1.numFields();
        int l2 = td2.numFields();
        int len = td1.numFields() + td2.numFields();
        Type[] types = new Type[len];
        String[] names = new String[len];
        for(int i = 0; i < len; i++){
            if(i < td1.numFields()){
                types[i] = td1.tdItems[i].fieldType;
                names[i] = td1.tdItems[i].fieldName;
            }else {
                types[i] = td2.tdItems[i-l1].fieldType;
                names[i] = td2.tdItems[i-l1].fieldName;
            }
        }
        return new TupleDesc(types,names);
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they have the same number of items
     * and if the i-th type in this TupleDesc is equal to the i-th type in o
     * for every i.
     *
     * @param o the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */

    public boolean equals(Object o) {
        if (o == null){
            return false;
        }
        if(this.getClass() != o.getClass()){
            return false;
        }
        if(tdItems.length != ((TupleDesc)o).tdItems.length){
            return false;
        }else{
            TDItem[] oitems = ((TupleDesc) o).tdItems;
            for(int i = 0; i < tdItems.length; i++){
                if(!tdItems[i].fieldType.equals(oitems[i].fieldType) || !tdItems[i].fieldName.equals(oitems[i].fieldName)){
                    return false;
                }
            }
        }
        return true;
    }

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        int result = 0;
        for (TDItem item : tdItems) {
            result += item.toString().hashCode() * 41 ;
        }
        return result;
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     *
     * @return String describing this descriptor.
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < tdItems.length; i++){
            if(i == tdItems.length-1){
                builder.append(tdItems[i].fieldType).append("(").append(tdItems[i].fieldName).append(")");
                continue;
            }
            builder.append(tdItems[i].fieldType).append("(").append(tdItems[i].fieldName).append(")").append(",");
        }
        return builder.toString();
    }
}
