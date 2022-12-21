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
public class TransactionTestTen extends SimpleDbTestBase {
    @Test public void testTenThreads()
            throws IOException, DbException, TransactionAbortedException {
        TransactionTestUtil.validateTransactions(10);
    }

    /** Make test compatible with older version of ant. */
    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TransactionTestTen.class);
    }
}
