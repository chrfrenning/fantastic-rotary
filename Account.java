public class Account {
    private double balance;

    public Account() {
        balance = 0;
    }

    public double getBalance() {
        System.err.println("Account::getBalance " + balance);
        return balance;
    }

    public void addInterest(double rate) {
        balance += balance * rate;
        System.err.println(String.format("Account::addInterest of %f, new balance %f", rate, balance));
    }

    public void deposit(double amount) {
        balance += amount;
        System.err.println(String.format("Account::deposit of %f, new balance %f", amount, balance));
    }

    public void withdraw(double amount) {
        if (balance >= amount) {
            balance -= amount;
            System.err.println(String.format("Account::withdraw of %f, new balance %f", amount, balance));
        } else {
            System.err.println("Insufficient funds");
        }
    }

    public void resetBalance(double amount) {
        balance = amount;
        System.err.println(String.format("Account::resetBalance of %f, new balance %f", amount, balance));
    }
}
