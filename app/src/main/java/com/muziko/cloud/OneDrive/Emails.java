
package com.muziko.cloud.OneDrive;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Emails {

    @SerializedName("preferred")
    @Expose
    private String preferred;
    @SerializedName("account")
    @Expose
    private String account;
    @SerializedName("personal")
    @Expose
    private Object personal;
    @SerializedName("business")
    @Expose
    private Object business;

    public String getPreferred() {
        return preferred;
    }

    public void setPreferred(String preferred) {
        this.preferred = preferred;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Object getPersonal() {
        return personal;
    }

    public void setPersonal(Object personal) {
        this.personal = personal;
    }

    public Object getBusiness() {
        return business;
    }

    public void setBusiness(Object business) {
        this.business = business;
    }

}
