
import java.time.LocalDate;
import java.util.*;

// 1. ACCOUNT BASE CLASS
class Account {
    private int accountNo;
    private String holderName;
    private double balance;
    private LocalDate openedDate;
    private List<String> statements;
    private static int totalAccounts = 0;

    public Account(int accountNo, String holderName, double balance) {
        if (balance < 0)
            throw new IllegalArgumentException("Invalid balance");

        this.accountNo = accountNo;
        this.holderName = holderName;
        this.balance = balance;
        this.openedDate = LocalDate.now();
        this.statements = new ArrayList<>();
        totalAccounts++;

        statements.add("Account opened: " + balance);
    }

    public int getAccountNo() { return accountNo; }
    public String getHolderName() { return holderName; }
    public double getBalance() { return balance; }
    public LocalDate getOpenedDate() { return openedDate; }

    public void setHolderName(String name) {
        holderName = name;
    }

    public static int getTotalAccounts() {
        return totalAccounts;
    }

    public void deposit(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount");

        balance += amount;
        statements.add("Deposit: +" + amount);
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance)
            return false;

        balance -= amount;
        statements.add("Withdrawal: -" + amount);
        return true;
    }

    public void transfer(Account target, double amount) {
        transfer(target, amount, "Account transfer");
    }

    public void transfer(Account target, double amount, String remark) {
        if (target == null || target == this)
            throw new IllegalArgumentException("Invalid target account");

        if (!withdraw(amount))
            throw new IllegalArgumentException("Transfer failed");

        target.deposit(amount);
        statements.add("Transfer: " + remark);
    }

    public void transfer(Account target, double amount,
                         LocalDate scheduledDate) {
        if (scheduledDate == null ||
            scheduledDate.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Invalid scheduled date");

        transfer(target, amount, "Scheduled: " + scheduledDate);
    }

    public void miniStatement() {
        System.out.println("\nMini Statement: " + accountNo);

        for (String s : statements)
            System.out.println(s);

        System.out.println("Balance: " + balance);
    }

    public String summary() {
        return "Account No: " + accountNo +
               ", Name: " + holderName +
               ", Balance: " + balance +
               ", Opened: " + openedDate;
    }

    protected void addCharge(double amount, String description) {
        balance -= amount;
        statements.add(description + ": -" + amount);
    }
}

// 2. SAVINGS ACCOUNT CLASS
class SavingsAccount extends Account {
    private double interestRate;
    private double minBalance;
    private String accountType;
    private boolean interestEnabled;

    public SavingsAccount(int no, String name, double balance,
                          double rate, double minBalance) {
        super(no, name, balance);
        this.interestRate = rate;
        this.minBalance = minBalance;
        this.accountType = "Savings";
        this.interestEnabled = true;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double rate) {
        if (rate < 0)
            throw new IllegalArgumentException("Invalid rate");
        interestRate = rate;
    }

    public double getMinBalance() {
        return minBalance;
    }

    public String getAccountType() {
        return accountType;
    }

    public boolean isInterestEnabled() {
        return interestEnabled;
    }

    public void setInterestEnabled(boolean value) {
        interestEnabled = value;
    }

    public void addInterest() {
        if (interestEnabled)
            deposit(getBalance() * interestRate / 100);
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0 || getBalance() - amount < minBalance)
            return false;

        return super.withdraw(amount);
    }
}

// 3. CURRENT ACCOUNT CLASS
class CurrentAccount extends Account {
    private double overdraftLimit;
    private double serviceCharge;
    private String accountType;
    private int freeTransactions;

    public CurrentAccount(int no, String name, double balance,
                          double overdraftLimit, double serviceCharge) {
        super(no, name, balance);
        this.overdraftLimit = overdraftLimit;
        this.serviceCharge = serviceCharge;
        this.accountType = "Current";
        this.freeTransactions = 5;
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(double limit) {
        overdraftLimit = limit;
    }

    public double getServiceCharge() {
        return serviceCharge;
    }

    public void setServiceCharge(double charge) {
        serviceCharge = charge;
    }

    public String getAccountType() {
        return accountType;
    }

    public int getFreeTransactions() {
        return freeTransactions;
    }

    public void applyServiceCharge() {
        if (serviceCharge > 0)
            addCharge(serviceCharge, "Service charge");
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0 ||
            amount > getBalance() + overdraftLimit)
            return false;

        return super.withdraw(amount) ||
               withdrawUsingOverdraft(amount);
    }

    private boolean withdrawUsingOverdraft(double amount) {
        if (amount > getBalance() + overdraftLimit)
            return false;

        addCharge(amount, "Overdraft withdrawal");
        return true;
    }
}

// 4. BANK SERVICE CLASS
class BankService {
    private Map<Integer, Account> accounts;
    private List<String> transactions;
    private String bankName;
    private int nextAccountNo;
    private LocalDate reportDate;

    public BankService(String bankName) {
        this.bankName = bankName;
        accounts = new LinkedHashMap<>();
        transactions = new ArrayList<>();
        nextAccountNo = 1001;
        reportDate = LocalDate.now();
    }

    public int generateAccountNo() {
        return nextAccountNo++;
    }

    public void addAccount(Account account) {
        if (accounts.containsKey(account.getAccountNo()))
            throw new IllegalArgumentException("Duplicate account");

        accounts.put(account.getAccountNo(), account);
        transactions.add("Account created: " +
                         account.getAccountNo());

        System.out.println("Account created successfully!");
    }

    public Account getAccount(int no) {
        return accounts.get(no);
    }

    public void listAccounts() {
        System.out.println("\n--- " + bankName + " Accounts ---");

        for (Account a : accounts.values())
            System.out.println(a.summary());
    }

    public void deposit(int no, double amount) {
        Account a = getAccount(no);

        if (a == null)
            throw new IllegalArgumentException("Account not found");

        a.deposit(amount);
        transactions.add("Deposit " + amount + " to " + no);
        System.out.println("Deposit successful!");
    }

    public void withdraw(int no, double amount) {
        Account a = getAccount(no);

        if (a == null || !a.withdraw(amount))
            throw new IllegalArgumentException("Withdrawal declined");

        transactions.add("Withdrawal " + amount + " from " + no);
        System.out.println("Withdrawal successful!");
    }

    public void transfer(int from, int to, double amount,
                         String remark) {
        Account source = getAccount(from);
        Account target = getAccount(to);

        if (source == null || target == null)
            throw new IllegalArgumentException("Account not found");

        source.transfer(target, amount, remark);

        transactions.add("Transfer " + amount +
                         " from " + from + " to " + to);

        System.out.println("Transfer successful!");
    }

    public void showMiniStatement(int no) {
        Account a = getAccount(no);

        if (a == null)
            System.out.println("Account not found");
        else
            a.miniStatement();
    }

    public void dailyTransactionReport() {
        System.out.println("\n--- Daily Transaction Report ---");
        System.out.println("Date: " + reportDate);

        for (String t : transactions)
            System.out.println(t);

        System.out.println("Total Transactions: " +
                           transactions.size());
    }

    public int getAccountCount() {
        return accounts.size();
    }

    public String getBankName() {
        return bankName;
    }
}

// 5. MAIN CLASS
public class Main {
    public static void main(String[] args) {

        BankService bank = new BankService("ABC Bank");

        // Create Savings Account
        SavingsAccount savings = new SavingsAccount(
            bank.generateAccountNo(),
            "Daniel",
            10000,
            4.0,
            1000
        );

        // Create Current Account
        CurrentAccount current = new CurrentAccount(
            bank.generateAccountNo(),
            "Barath",
            15000,
            5000,
            100
        );

        // Add Accounts
        bank.addAccount(savings);
        bank.addAccount(current);

        // Polymorphism
        List<Account> accountList = new ArrayList<>();
        accountList.add(savings);
        accountList.add(current);

        // Display Accounts
        bank.listAccounts();

        // Deposit
        System.out.println("\n--- Deposit ---");
        bank.deposit(savings.getAccountNo(), 2000);

        // Withdrawal
        System.out.println("\n--- Withdrawal ---");
        bank.withdraw(current.getAccountNo(), 1000);

        // Transfer
        System.out.println("\n--- Transfer ---");
        bank.transfer(
            savings.getAccountNo(),
            current.getAccountNo(),
            2500,
            "Monthly Payment"
        );

        // Add Interest
        System.out.println("\n--- Interest ---");
        savings.addInterest();

        // Account Summary
        System.out.println("\n--- Account Summaries ---");

        for (Account account : accountList)
            System.out.println(account.summary());

        // Mini Statements
        bank.showMiniStatement(savings.getAccountNo());
        bank.showMiniStatement(current.getAccountNo());

        // Daily Report
        bank.dailyTransactionReport();

        System.out.println("\nTotal Accounts: " +
                           Account.getTotalAccounts());

        System.out.println("\nBanking Application Completed.");
    }
}
