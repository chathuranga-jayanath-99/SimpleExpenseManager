/*
 * Copyright 2015 Department of Computer Science and Engineering, University of Moratuwa.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *                  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package lk.ac.mrt.cse.dbs.simpleexpensemanager;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.control.ExpenseManager;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.control.PersistentExpenseManager;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.control.exception.ExpenseManagerException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.PersistentAccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.PersistentTransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest {
    private static ExpenseManager expenseManager;
    private static PersistentAccountDAO persistentAccountDAO;
    private static PersistentTransactionDAO persistentTransactionDAO;

    @BeforeClass
    public static void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        persistentAccountDAO = new PersistentAccountDAO(context);
        persistentTransactionDAO = new PersistentTransactionDAO(context);

        expenseManager = new PersistentExpenseManager(context);
    }

    @Test
    public void addAccountTest() throws InvalidAccountException {
        String accountNo = "1000";
        List<String> accountsListBefore = expenseManager.getAccountNumbersList();
        if (accountsListBefore.contains(accountNo)){
            persistentAccountDAO.removeAccount(accountNo);
        }
        expenseManager.addAccount(accountNo, "BOC", "Supun", 2000.00);

        List<String> accountsListAfter = expenseManager.getAccountNumbersList();
        assertTrue(accountsListAfter.contains(accountNo));
    }

    @Test
    public void removeAccountTest() throws InvalidAccountException {
        String accountNo = "1100";

        Account account = new Account(accountNo, "BOC", "Sahan", 1450.00);
        List<String> accountsListBefore = expenseManager.getAccountNumbersList();
        if (!accountsListBefore.contains(accountNo)){
            persistentAccountDAO.addAccount(account);
        }

        persistentAccountDAO.removeAccount(accountNo);
        List<String> accountNumbersListAfter = persistentAccountDAO.getAccountNumbersList();
        assertFalse(accountNumbersListAfter.contains(accountNo));
    }

    @Test
    public void addTransactionLogTest(){
        int noOfTransactions = persistentTransactionDAO.getAllTransactionLogs().size();
        try {
            expenseManager.updateAccountBalance("1001", 24, 5, 2018, ExpenseType.INCOME, "2000.0");
            assertEquals(noOfTransactions + 1, persistentTransactionDAO.getAllTransactionLogs().size());
        } catch (InvalidAccountException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getAccountNumbersListTest() throws InvalidAccountException {
        String accountNo = "2000";
        List<String> accountsListBefore = expenseManager.getAccountNumbersList();

        expenseManager.addAccount(accountNo, "BOC", "Sampath", 2000.00);

        List<String> accountsListAfter = expenseManager.getAccountNumbersList();
        assertEquals(accountsListAfter.size(), accountsListBefore.size() + 1);
    }
}