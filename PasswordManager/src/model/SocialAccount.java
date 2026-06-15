package model;

/**
 * SocialAccount - represents a social media account (Instagram, Twitter, etc.)
 */
public class SocialAccount extends Account {

    private String platform; 
    public SocialAccount(String website, String username, String password, String platform, String tag) {
        super(website, username, password, tag);
        this.platform = platform;
    }

    public String getPlatform() { return platform; }

    @Override
    public String getType() { return "Social Media"; }

    @Override
    public String toCSV() {
        return super.toCSV() + "," + platform;
    }
}
