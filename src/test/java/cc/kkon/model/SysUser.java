package cc.kkon.model;

import java.util.Date;

public class SysUser {

    private String str1;

    private String str2;

    private Integer int1;

    private Date date1;


    public String getStr1() {
        return str1;
    }

    public SysUser setStr1(String str1) {
        this.str1 = str1;
        return this;
    }

    public String getStr2() {
        return str2;
    }

    public SysUser setStr2(String str2) {
        this.str2 = str2;
        return this;
    }

    public Integer getInt1() {
        return int1;
    }

    public SysUser setInt1(Integer int1) {
        this.int1 = int1;
        return this;
    }

    public Date getDate1() {
        return date1;
    }

    public SysUser setDate1(Date date1) {
        this.date1 = date1;
        return this;
    }

    @Override
    public String toString() {
        return "SysUser{" +
                "str1='" + str1 + '\'' +
                ", str2='" + str2 + '\'' +
                ", int1=" + int1 +
                ", date1=" + date1 +
                '}';
    }
}
