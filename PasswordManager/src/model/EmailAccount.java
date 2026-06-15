package model;

/**
 * EmailAccount - represents an email account (Gmail, Outlook, etc.)
 * Adds a recoveryEmail field for extra info.
 */
public class EmailAccount extends Account {

    private String recoveryEmail;

    public EmailAccount(String website, String username, String password, String recoveryEmail, String tag) {
        super(website, username, password, tag);
        this.recoveryEmail = recoveryEmail;
    }

    public String getRecoveryEmail() { return recoveryEmail; }

    @Override
    public String getType() { return "Email"; }

    @Override
    public String toCSV() {
        return super.toCSV() + "," + recoveryEmail;
    }
}
