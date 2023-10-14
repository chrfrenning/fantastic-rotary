public class Account {
    private double balance;

    public Account() {
        balance = 0;
    }

    public double getBalance() {
        System.out.println("Account::getBalance " + balance);
        return balance;
    }

    public void addInterest(double rate) {
        balance += balance * rate;
        System.out.println(String.format("Account::addInterest of %f, new balance %f", rate, balance));
    }

    public void deposit(double amount) {
        balance += amount;
        System.out.println(String.format("Account::deposit of %f, new balance %f", amount, balance));
    }

    public void withdraw(double amount) {
        if (balance >= amount) {
            balance -= amount;
            System.out.println(String.format("Account::withdraw of %f, new balance %f", amount, balance));
        } else {
            System.out.println("Insufficient funds");
        }
    }
}
