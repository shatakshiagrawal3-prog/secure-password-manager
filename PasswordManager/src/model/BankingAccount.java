package model;

/**
 * BankingAccount - represents a banking/finance account.
 * Adds bankName field.
 */
public class BankingAccount extends Account {

    private String bankName;

    public BankingAccount(String website, String username, String password, String bankName, String tag) {
        super(website, username, password, tag);
        this.bankName = bankName;
    }

    public String getBankName() { return bankName; }

    @Override
    public String getType() { return "Banking"; }

    @Override
    public String toCSV() {
        return super.toCSV() + "," + bankName;
    }
}
