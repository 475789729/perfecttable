package com.liuyao.perfecttable;

import java.util.List;

public class TableData<T> {

    public TableData(List<T> rowDataList){
        this.rowDataList = rowDataList;
    }
    private List<T> rowDataList;


    public List<T> getRowDataList() {
        return rowDataList;
    }

    public void setRowDataList(List<T> rowDataList) {
        this.rowDataList = rowDataList;
    }


}
