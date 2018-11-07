package com.liuyao.perfecttable;

public class Column implements IColumn{
     private String columnName;
     //多级列名支持，index 0是最上面，然后一级一级往下面排
     private String[] parentName;
     //反射取值
     private String property;
     //是否固定列，不许左右滑动
     private boolean fix;

     private Integer minWidthDp;
     private Integer maxWidthDp;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String[] getParentName() {
        return parentName;
    }

    public void setParentName(String[] parentName) {
        this.parentName = parentName;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public boolean isFix() {
        return fix;
    }

    public void setFix(boolean fix) {
        this.fix = fix;
    }


    public Integer getMinWidthDp() {
        return minWidthDp;
    }

    public void setMinWidthDp(Integer minWidthDp) {
        this.minWidthDp = minWidthDp;
    }

    public Integer getMaxWidthDp() {
        return maxWidthDp;
    }

    public void setMaxWidthDp(Integer maxWidthDp) {
        this.maxWidthDp = maxWidthDp;
    }
}
