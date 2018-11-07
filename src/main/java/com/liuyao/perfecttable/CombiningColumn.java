package com.liuyao.perfecttable;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 列的header可以有多级展示
 *      |                课程              |
 *      |          软件  |        硬件     |
 *      |  java |  php   |  单片机 |  芯片 |
 */
public class CombiningColumn implements IColumn{
    //最底层节点，其实是普通Column
     private List<Column> columnList;
     //上层节点其实是一些标题，最外层list表示列名的题目从上到下分的级，内层list表示同一级包含了多少的名字
    private List<List<LevelTitle>> columnLevelTitle;

    public List<Column> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<Column> columnList) {
        this.columnList = columnList;
    }

    public List<List<LevelTitle>> getColumnLevelTitle() {
        return columnLevelTitle;
    }

    public void setColumnLevelTitle(List<List<LevelTitle>> columnLevelTitle) {
        this.columnLevelTitle = columnLevelTitle;
    }
    private CombiningColumn(){}

    /**
     * 传入需要合并的列，会生产一个合并列对象，内部是一些数学逻辑
     * @param columns
     * @return
     */
    public static  CombiningColumn produce(List<Column> columns){
           checkValid(columns);

           CombiningColumn combiningColumn = new CombiningColumn();
           combiningColumn.setColumnList(columns);
           List<List<LevelTitle>> allTopTitleLevel = new ArrayList<List<LevelTitle>>();
           List<LevelTitle>  mostTopTitle = new ArrayList<LevelTitle>();
           LevelTitle top = new LevelTitle();
           top.setColumnIndexBegin(0);
           top.setColumnIndexEnd(columns.size());
           top.setName(columns.get(0).getParentName()[0]);
           mostTopTitle.add(top);
           allTopTitleLevel.add(mostTopTitle);
           //计算出其余层的LevelTitle信息
           for(int i = 1; i < columns.get(0).getParentName().length; i++){
               collectEachLevel(allTopTitleLevel, columns, i);
           }
           combiningColumn.setColumnLevelTitle(allTopTitleLevel);
           //搞定了父列名，再把真正的子列进行排序
           sortRealColumn(columns, allTopTitleLevel);
           return combiningColumn;
    }

    /**
     * 把真正的子列进行排序
     * @param columns
     */
    private static void sortRealColumn(List<Column> columns, List<List<LevelTitle>> allTopTitleLevel){

        Column[] newSort = new Column[columns.size()];
        for(int i = 0; i < columns.size(); i++){
            Column column = columns.get(i);
            for(int j = 0; j < newSort.length; j++){
                if(newSort[j] == null){
                    if(canSitDown(column, allTopTitleLevel, j, allTopTitleLevel.size())){
                        newSort[j] = column;
                        break;
                    }
                }
            }
        }
        columns.clear();
        Collections.addAll(columns, newSort);

    }
    /**
     * 计算出这一层的List<LevelTitle>
     * @param allTopTitleLevel
     * @param columns
     * @param levelIndex
     */
    private static void collectEachLevel(List<List<LevelTitle>> allTopTitleLevel, List<Column> columns, final int levelIndex){
        Collections.sort(columns, new Comparator<Column>() {
            @Override
            public int compare(Column o1, Column o2) {
                return o1.getParentName()[levelIndex].compareTo(o2.getParentName()[levelIndex]);
            }
        });
        String[] tempSeat = new String[columns.size()];
        for(int i = 0; i < columns.size(); i++){
             Column column = columns.get(i);
            for(int j = 0; j < tempSeat.length; j++){
                 if(TextUtils.isEmpty(tempSeat[j])){
                     if(canSitDown(column, allTopTitleLevel, j, levelIndex)){
                          tempSeat[j] = column.getParentName()[levelIndex];
                          break;
                     }
                 }
            }
        }
        List<LevelTitle> thisLevelTitles = new ArrayList<LevelTitle>();
        for(int m = 0; m < tempSeat.length; m++){
             if(m == 0){
                  LevelTitle levelTitle = new LevelTitle();
                  levelTitle.setName(tempSeat[m]);
                  levelTitle.setColumnIndexBegin(0);
                  levelTitle.setColumnIndexEnd(1);
                 thisLevelTitles.add(levelTitle);
                 continue;
             }else{
                    if(tempSeat[m].equals(tempSeat[m - 1])){
                        int newEnd = thisLevelTitles.get(thisLevelTitles.size() - 1).getColumnIndexEnd() + 1;
                        thisLevelTitles.get(thisLevelTitles.size() - 1).setColumnIndexEnd(newEnd);
                    }else{
                        LevelTitle levelTitle = new LevelTitle();
                        levelTitle.setName(tempSeat[m]);
                        levelTitle.setColumnIndexBegin(m);
                        levelTitle.setColumnIndexEnd(m + 1);
                        thisLevelTitles.add(levelTitle);
                    }
             }
        }
        allTopTitleLevel.add(thisLevelTitles);
    }

    /**
     * 辅助计算
     * @param column
     * @param allTopTitleLevel
     * @param seatIndex
     * @param needCheckLevel
     * @return
     */
    private static boolean canSitDown(Column column, List<List<LevelTitle>> allTopTitleLevel, int seatIndex, int needCheckLevel){
              for(int i = 0; i < needCheckLevel; i++){
                     String checkString = computeCheckString(allTopTitleLevel, i, seatIndex);
                     if(!checkString.equals(column.getParentName()[i])){
                         return false;
                     }
              }
              return true;
    }

    /**
     * 辅助计算
     * @param allTopTitleLevel
     * @param level
     * @param seatIndex
     * @return
     */
    private static String computeCheckString(List<List<LevelTitle>> allTopTitleLevel, int level, int seatIndex){
        List<LevelTitle> thisLevel = allTopTitleLevel.get(level);
        for(int i = 0; i < thisLevel.size(); i++){
            LevelTitle levelTitleItem = thisLevel.get(i);
            if(seatIndex >= levelTitleItem.getColumnIndexBegin() && seatIndex < levelTitleItem.getColumnIndexEnd()){
                return levelTitleItem.getName();
            }
        }
        throw new RuntimeException("unknow error");
    }

    /**
     * 检验是否可以合并
     * @param columns
     * @return
     */
    private static void checkValid(List<Column> columns){
        String topName = null;
        int level = 0;
        for(int i = 0; i < columns.size(); i++){
            Column column = columns.get(i);
            if(column.isFix()){
                throw new RuntimeException("Combining column not support fix,use setFix(false)");
            }
            if(column.getParentName() == null || column.getParentName().length == 0){
                throw new RuntimeException("when Combining column, each column mush have parentName ");
            }else{
                for(int n = 0; n < column.getParentName().length; n++){
                    if(TextUtils.isEmpty(column.getParentName()[n])){
                        throw new RuntimeException("Column set parentName, but text is null");
                    }
                }
                if(TextUtils.isEmpty(topName)){
                    topName = column.getParentName()[0];
                    level = column.getParentName().length;
                }else{
                    if(column.getParentName().length != level || !topName.equals(column.getParentName()[0])){
                        throw new RuntimeException("these column can't combining");
                    }
                }
            }
        }

    }

    public static class  LevelTitle{
        //横跨的列索引
        //开始索引，包含
        private int columnIndexBegin;
        //结束索引，不包含
        private int columnIndexEnd;
        //这一级的列名字
        private String name;

        public int getColumnIndexBegin() {
            return columnIndexBegin;
        }

        public void setColumnIndexBegin(int columnIndexBegin) {
            this.columnIndexBegin = columnIndexBegin;
        }

        public int getColumnIndexEnd() {
            return columnIndexEnd;
        }

        public void setColumnIndexEnd(int columnIndexEnd) {
            this.columnIndexEnd = columnIndexEnd;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
