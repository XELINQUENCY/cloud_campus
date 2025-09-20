package client.bank;

import client.ApiException;
import entity.bank.BankAccount;
import entity.bank.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IBankClientSrv {
    // 用户操作
    boolean login(String userId, String password) throws ApiException;
    boolean register(String userId, String password) throws ApiException;

    // 账户操作
    BankAccount createAccount() throws ApiException;

    // 交易操作
    boolean deposit(String accountId, BigDecimal amount) throws ApiException;
    boolean withdraw(String accountId, BigDecimal amount, String password) throws ApiException;
    boolean transfer(String fromAccountId, String toAccountId, BigDecimal amount, String password) throws ApiException;

    List<Transaction> getTransactions(String accountId, LocalDateTime start, LocalDateTime end) throws ApiException;
    List<BankAccount> getUserAccounts(String userId) throws ApiException;
}
