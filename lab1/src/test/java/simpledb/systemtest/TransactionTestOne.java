package simpledb.systemtest;

import org.junit.Test;
import simpledb.common.DbException;
import simpledb.transaction.TransactionAbortedException;

import java.io.IOException;

/**
 * Tests running concurrent transactions.
 * You do not need to pass this test until Lab 4.
 * @see TransactionTestUtil
 */
public class TransactionTestOne extends SimpleDbTestBase {
    @Test public void testSingleThread()
            throws IOException, DbException, TransactionAbortedException {
        TransactionTestUtil.validateTransactions(1);
    }

    /** Make test compatible with older version of ant. */
    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TransactionTestOne.class);
    }
}
