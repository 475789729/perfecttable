package com.liuyao.perfecttable;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerfectTableViewBuilder<T> {
         private Context context;
         private int cellHorizontalPaddingDp = 5;
         private int cellVerticalPaddingDp = 3;
         private int borderWidthPx = 2;
         private String borderColorString = "#d9d9d9";
         private int defaultMaxColumnWidthDp = 150;
          private int defaultMinColumnWidthDp = 30;
         private List<Column> columnList;

         private String cellBackgroundColor;
         private String columnHeaderBackgroundColor;
         private String rowHeaderBackgroundColor;

         private String cellTextColor = "#a6000000";
         private String columnHeaderTextColor;


         private int textSizeDp = 16;
         private int textSizeGap = 2;

         private int paddingGap = 1;


         private PerfectTableView.RowHeaderViewFactory<T> rowHeaderViewFactory;



        public PerfectTableViewBuilder(Context context){
              this.context = context;
        }
        public PerfectTableViewBuilder setCellHorizontalPaddingDp(int cellHorizontalPaddingDp){
            this.cellHorizontalPaddingDp = cellHorizontalPaddingDp;
            return this;
        }

        public PerfectTableViewBuilder setCellVerticalPaddingDp(int cellVerticalPaddingDp){
            this.cellVerticalPaddingDp = cellVerticalPaddingDp;
            return this;
        }

       public PerfectTableViewBuilder setColumnList(@NonNull List<Column> columnList){
           this.columnList = columnList;
            if(columnList.size() == 0){
                throw new RuntimeException("columnList.size() must > 0");
            }
            for(int i = 0; i < columnList.size(); i++){
                checkColumn(columnList.get(i));
            }
           return this;
       }

      public PerfectTableViewBuilder setBorderWidthPx(int borderWidthPx){
          this.borderWidthPx = borderWidthPx;
           return this;
      }

    /**
     *注意，不要带透明度的颜色，使用rgb，禁止使用argb，否则某些局部边框叠加在一起会产生色差
     * @param borderColorString  例如"#000000"
     * @return
     */
     public PerfectTableViewBuilder setBorderColorString(String borderColorString){
         this.borderColorString = borderColorString;
         return this;
     }

     public PerfectTableViewBuilder setCellBackgroundColor(String colorString){
         this.cellBackgroundColor = colorString;
          return this;
     }

      public PerfectTableViewBuilder setColumnHeaderBackgroundColor(String colorString){
          this.columnHeaderBackgroundColor = colorString;
          return this;
      }

      public PerfectTableViewBuilder setRowHeaderBackgroundColor(String colorString){
          this.rowHeaderBackgroundColor = colorString;
          return this;
      }

      public PerfectTableViewBuilder setCellTextColor(String colorString){
          this.cellTextColor = colorString;
          return this;
      }

      public PerfectTableViewBuilder setColumnHeaderTextColor(String colorString){
          this.columnHeaderTextColor = colorString;
          return this;
      }



      public PerfectTableViewBuilder setTextSizeDp(int textSizeDp){
          this.textSizeDp = textSizeDp;
          return this;
      }

      public PerfectTableViewBuilder setRowHeaderViewFactory(PerfectTableView.RowHeaderViewFactory rowHeaderViewFactory){
          this.rowHeaderViewFactory = rowHeaderViewFactory;
          return this;
      }

      public PerfectTableView<T> create(){
          PerfectTableView<T> tableView = new PerfectTableView<T>(context);
          this.borderWidthPx = Math.max(0, this.borderWidthPx);
          tableView.setBorderWidth(this.borderWidthPx);
          tableView.setBorderColorString(this.borderColorString);
          tableView.setCellHorizontalPadding(rulePadding(this.cellHorizontalPaddingDp));
          tableView.setCellVerticalPadding(rulePadding(this.cellVerticalPaddingDp));
          tableView.setSecondHorizontalPadding(rulePadding(this.cellHorizontalPaddingDp - paddingGap));
          tableView.setSecondVerticalPadding(rulePadding(this.cellVerticalPaddingDp - paddingGap));
          tableView.setThirdHorizontalPadding(rulePadding(this.cellHorizontalPaddingDp - paddingGap * 2));
          tableView.setThirdVerticalPadding(rulePadding(this.cellVerticalPaddingDp - paddingGap * 2));
          tableView.setCellBackgroundColor(this.cellBackgroundColor);
          tableView.setColumnHeaderBackgroundColor(this.columnHeaderBackgroundColor);
          tableView.setRowHeaderBackgroundColor(this.rowHeaderBackgroundColor);
          tableView.setCellTextColor(this.cellTextColor);
          tableView.setColumnHeaderTextColor(this.columnHeaderTextColor);
          tableView.setRowHeaderViewFactory(this.rowHeaderViewFactory);
          tableView.setTextSizeDp(textSizeDp);
          tableView.setSecondTextSizeDp(textSizeDp - textSizeGap);
          tableView.setThirdTextSizeDp(textSizeDp - textSizeGap - textSizeGap);


              if(this.columnList == null){
                  throw new RuntimeException("columnList is null, can not create table view");
              }
              tableView.setColumnDefine(makeCombiningColumns());

          return tableView;
      }
       private int rulePadding(int padding){
           return Math.max(padding, specialPx2dip((float) borderWidthPx));
       }
       private void checkColumn(Column column){
           if(TextUtils.isEmpty(column.getColumnName())){
                throw new RuntimeException("columnName is null");
           }

           if(TextUtils.isEmpty(column.getProperty())){
               throw  new RuntimeException("property is null");
           }
           if(column.getParentName() != null){
                if(column.getParentName().length == 0){
                     column.setParentName(null);
                }else{
                    for(String title : column.getParentName()){
                        if(TextUtils.isEmpty(title)){
                            throw  new RuntimeException("parenetName is empty");
                        }
                    }
                }

           }

           if(column.getParentName() != null && column.isFix()){
               throw new RuntimeException("sorry, column that have parentName cannot setFix(true)");
           }
           if(column.getMaxWidthDp() == null){
               column.setMaxWidthDp(defaultMaxColumnWidthDp);
           }
           if(column.getMinWidthDp() == null){
               column.setMinWidthDp(defaultMinColumnWidthDp);
           }
       }

    /**
     * 不四舍五入，直接+1
     * @param pxValue
     * @return
     */
    private int specialPx2dip(float pxValue){
           final float scale = context.getResources().getDisplayMetrics().density;
           return (int) (pxValue / scale + 0.96f);
       }


       private List<IColumn> makeCombiningColumns(){
           Map<String, List<Column>> map = new HashMap<String, List<Column>>();
           for(int i = 0; i < this.columnList.size(); i++){
                Column column = this.columnList.get(i);
                if(column.getParentName() != null){
                    String tag = String.valueOf(column.getParentName().length) + column.getParentName()[0];
                    if(map.get(tag) == null){
                            List<Column> columnArray = new ArrayList<Column>();
                            columnArray.add(column);
                            map.put(tag, columnArray);
                    }else{
                        List<Column> columnArray = map.get(tag);
                        columnArray.add(column);
                    }

                }
           }
           List<IColumn> result = new ArrayList<IColumn>();
            for(int i = 0; i < this.columnList.size(); i++){
                Column column = this.columnList.get(i);
                if(column.getParentName() != null){
                    String tag = String.valueOf(column.getParentName().length) + column.getParentName()[0];
                    if(map.get(tag) != null){
                         result.add(CombiningColumn.produce(map.get(tag)));
                         map.remove(tag);
                    }
                }else{
                    result.add(column);
                }
           }
           return result;
       }

}
