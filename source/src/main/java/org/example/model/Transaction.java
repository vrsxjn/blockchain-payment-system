package org.example.model;

import java.util.List;

public class Transaction {
    private String accountId;
    private String stakeholderId;
    private String date;
    private String description;
    private String reference;
    private boolean isFlag;
    private String accrualDate;
    private List<Category> categories;
    private List<CostCenter> costcenters;


    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getStakeholderId() {
        return stakeholderId;
    }

    public void setStakeholderId(String stakeholderId) {
        this.stakeholderId = stakeholderId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean isFlag() {
        return isFlag;
    }

    public void setFlag(boolean flag) {
        isFlag = flag;
    }

    public String getAccrualDate() {
        return accrualDate;
    }

    public void setAccrualDate(String accrualDate) {
        this.accrualDate = accrualDate;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<CostCenter> getCostcenters() {
        return costcenters;
    }

    public void setCostcenters(List<CostCenter> costcenters) {
        this.costcenters = costcenters;
    }

    public static class Category {
        private String categoryid;
        private double value;
        private String description;

        // Getters and Setters

        public String getCategoryid() {
            return categoryid;
        }

        public void setCategoryid(String categoryid) {
            this.categoryid = categoryid;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class CostCenter {
        private String costcenterid;
        private int percent;
        private String description;

        // Getters and Setters

        public String getCostcenterid() {
            return costcenterid;
        }

        public void setCostcenterid(String costcenterid) {
            this.costcenterid = costcenterid;
        }

        public int getPercent() {
            return percent;
        }

        public void setPercent(int percent) {
            this.percent = percent;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
