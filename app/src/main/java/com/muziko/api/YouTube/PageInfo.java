package com.muziko.api.YouTube;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class PageInfo {

    @SerializedName("totalResults")
    @Expose
    private int totalResults;

    @SerializedName("resultsPerPage")
    @Expose
    private int resultsPerPage;

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public int getResultsPerPage() {
        return resultsPerPage;
    }

    public void setResultsPerPage(int resultsPerPage) {
        this.resultsPerPage = resultsPerPage;
    }

    @Override
    public String toString() {
        return
                "PageInfo{" +
                        "totalResults = '" + totalResults + '\'' +
                        ",resultsPerPage = '" + resultsPerPage + '\'' +
                        "}";
    }
}