package model;

/**
 * WorkAccount - represents a work/office account.
 */
public class WorkAccount extends Account {

    private String companyName;

    public WorkAccount(String website, String username, String password, String companyName, String tag) {
        super(website, username, password, tag);
        this.companyName = companyName;
    }

    public String getCompanyName() { return companyName; }

    @Override
    public String getType() { return "Work"; }

    @Override
    public String toCSV() {
        return super.toCSV() + "," + companyName;
    }
}
