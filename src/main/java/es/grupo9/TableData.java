package es.grupo9;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class TableData {
    SimpleStringProperty member;
    SimpleDoubleProperty activities;
    SimpleDoubleProperty hours;
    SimpleDoubleProperty cost;
    SimpleStringProperty branch;
    SimpleStringProperty description;
    SimpleStringProperty date;
    SimpleStringProperty tag;
    SimpleStringProperty date2;

    TableData(String branch, String description, String date){
        this.branch= new SimpleStringProperty(branch);
        this.description= new SimpleStringProperty(description);
        this.date= new SimpleStringProperty(date);
    }

    TableData(String tag, String date2){
        this.tag = new SimpleStringProperty(tag);
        this.date2 = new SimpleStringProperty(date2);
    }

    TableData(String member, Double activities, Double hours, Double cost) {
        this.member = new SimpleStringProperty(member);
        this.activities = new SimpleDoubleProperty(activities);
        this.hours = new SimpleDoubleProperty(hours);
        this.cost = new SimpleDoubleProperty(cost);
    }

    public String getMember() {
        return member.get();
    }

    public void setMember(String m) {
        member.set(m);
    }

    public Double getActivities() {
        return activities.get();
    }

    public void setActivities(Double e) {
        activities.set(e);
    }

    public Double getHours() {
        return hours.get();
    }

    public void setHours(Double h) {
        hours.set(h);
    }

    public Double getCost() {
        return cost.get();
    }

    public void setCost(Double c) {
        cost.set(c);
    }

    public String getDate() { return date.get();}

    public void setDate(String p) {
        date.set(p);
    }

    public String getBranch() {
        return branch.get();
    }

    public void setBranch(String b) {
        branch.set(b);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String d) {
        description.set(d);
    }

    public String getTag() { return tag.get(); }

    public void setTag(String l) {
        tag.set(l);
    }

    public String getDate2() { return date2.get();}

    public void setDate2(String p) {
        date2.set(p);
    }

}