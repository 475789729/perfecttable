package com.liuyao.perfecttable;

import android.content.Context;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PerfectTableView<T> extends LinearLayout {

    private TableData<T> tableData;
    private View connerView;
    private LinearLayout fixColumnHeader;
    private ColumnHeaderScrollView columnHeaderView;
    private LinearLayout columnHeaderView_container;
    private RowHeaderScrollView rowHeaderAndFixColoumCell;
    private CustomLinearLayout cell_content;
    private LinearLayout rowHeaderAndFixColoumCell_child;
    //解决:设置rowHeaderBackground并且没有connerView并且没有固定列情况下border缺失了一点点的bug。用一个多余的view来遮罩一下，调制成border相同的颜色和尺寸
    private LinearLayout lostBorderView;

    //所有子列平铺，fix列应该排序提前，组合列不支持fix属性
    private List<Column> realColumnList;
    //所有列，可能是组合列类型，fix列(不可能是组合列)应该排序提前
    private List<IColumn> columnList;

    private int  cellHorizontalPaddingDp= 3;
    private int cellVerticalPaddingDp = 3;

    private int secondHorizontalPaddingDp = 2;
    private int secondVerticalPaddingDp = 2;

    private int thirdHorizontalPaddingDp = 1;
    private int thirdVerticalPaddingDp = 1;

    private int minCellHeightDp = 20;


    //不要带透明度，不要用argb，用rgb
    private String borderColorString = "#d9d9d9";
    private int borderWidth = 2;

    private String cellBackgroundColor = null;
    private String columnHeaderBackgroundColor = null;
    private String rowHeaderBackgroundColor = null;

    private String cellTextColor = "#a6000000";
    private String columnHeaderTextColor;


    private int textSizeDp = 16;
    private int secondTextSizeDp = 14;
    private int thirdTextSizeDp = 12;

    public static final String LTRB = "LTRB";
    public static final String RB = "RB";
    public static final String TRB = "TRB";
    public static final String LRB = "LRB";

    private RowHeaderViewFactory rowHeaderViewFactory;

    private boolean attachWindow = false;




    public PerfectTableView(Context context) {
        super(context);
        init();
    }

    public PerfectTableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        LayoutInflater.from(getContext()).inflate(R.layout.layout_perfecttable, this, true);
        this.setOrientation(VERTICAL);

        initView();
    }

    private void initView(){
        lostBorderView = (LinearLayout) findViewById(R.id.lostBorderView);
        connerView = findViewById(R.id.conner_view);
        fixColumnHeader = (LinearLayout) findViewById(R.id.fixColumnHeader);
        columnHeaderView = (ColumnHeaderScrollView) findViewById(R.id.columnHeaderView);
        rowHeaderAndFixColoumCell = (RowHeaderScrollView) findViewById(R.id.rowHeaderAndFixColoumCell);
        cell_content = (CustomLinearLayout) findViewById(R.id.cell_content);
        cell_content.setColumnHeaderScrollView(columnHeaderView);
        cell_content.setRowHeaderScrollView(rowHeaderAndFixColoumCell);
        columnHeaderView_container = (LinearLayout) findViewById(R.id.columnHeaderView_container);
        rowHeaderAndFixColoumCell_child = (LinearLayout) findViewById(R.id.rowHeaderAndFixColoumCell_child);

        fixColumnHeader.setBackground(BackgroundDrawableCreater.getBorderDrawable(borderWidth, borderColorString, LTRB));
         connerView.setBackground(BackgroundDrawableCreater.getBorderDrawable(borderWidth, borderColorString, LTRB, rowHeaderBackgroundColor));
    }

    /**
     *
     * @param source
     */
    public void setColumnDefine(List<IColumn> source){
        if(this.columnList != null){
            throw new RuntimeException("already set columns，can't set again");
        }
        this.columnList = new ArrayList<IColumn>();
        this.realColumnList = new ArrayList<Column>();
        //fix列提前
        for(int i = 0; i < source.size(); i++){
            IColumn iColumn = source.get(i);
            if(iColumn instanceof Column && ((Column) iColumn).isFix()){
                columnList.add(iColumn);
            }
        }
        //再加非fix列
        for(int i = 0; i < source.size(); i++){
            IColumn iColumn = source.get(i);
            if(iColumn instanceof CombiningColumn){
                columnList.add(iColumn);
            }else{
                if(!((Column) iColumn).isFix()){
                    columnList.add(iColumn);
                }
            }
        }

          //再生成纯子列的数组
        for(int i = 0; i < columnList.size(); i++){
            IColumn iColumn = columnList.get(i);
            if(iColumn instanceof Column){
                realColumnList.add((Column)iColumn);
            }else{
                realColumnList.addAll(((CombiningColumn) iColumn).getColumnList());
            }
        }

        //生成列headerView
         for(int i = 0; i < columnList.size(); i++){
               IColumn iColumn = columnList.get(i);
               if(iColumn instanceof Column && ((Column) iColumn).isFix()){
                   fixColumnHeader.addView(createOneColumnHeaderCellView((Column) iColumn));
               }else{
                   if(iColumn instanceof Column){
                       columnHeaderView_container.addView(createOneColumnHeaderCellView((Column) iColumn));
                   }else{
                       columnHeaderView_container.addView(createOneColumnHeaderCellView((CombiningColumn) iColumn));
                   }

               }
         }
         autoFitWidthAndHeight();

        fixColumnHeader.setBackground(BackgroundDrawableCreater.getBorderDrawable(borderWidth, borderColorString, LTRB));
        connerView.setBackground(BackgroundDrawableCreater.getBorderDrawable(borderWidth, borderColorString, LTRB, rowHeaderBackgroundColor));
        this.setBackground(BackgroundDrawableCreater.getBorderDrawable(borderWidth, borderColorString, LTRB));

    }

    /**
     * 表格有一些border缺失，这个方法会充分考虑各种情况，尽量补全border
     * 解决各种边框不对的bug
     * 目前发现的bug:1.当设置了rowHeaderBackground并且没有connerView并且没有固定列情况下border缺失
     *
     */
    private void drawLostBorder(){
        lostBorderView.setVisibility(GONE);
        if(rowHeaderViewFactory == null || tableData == null){
              boolean hasFixColumn = fixColumnHeader.getChildCount() > 0;
              if(hasFixColumn){
                  fixColumnHeader.getChildAt(0).setBackground(BackgroundDrawableCreater.getBorderDrawable(borderWidth, borderColorString, LTRB, columnHeaderBackgroundColor));
              }else{
                  //只能弄个view遮罩一下。。。没其他办法
                  if(columnHeaderView_container.getChildCount() > 0){
                      lostBorderView.setVisibility(VISIBLE);
                      lostBorderView.getLayoutParams().width = borderWidth;
                      lostBorderView.setBackgroundColor(Color.parseColor(borderColorString));
                      lostBorderView.getLayoutParams().height = calculateLostBorderHeight();
                      lostBorderView.setLayoutParams(lostBorderView.getLayoutParams());
                  }

              }
        }else if(rowHeaderViewFactory != null && tableData != null && tableData.getRowDataList() != null && tableData.getRowDataList().size() > 0){
            //有connerView的情况下，不要造成局部border加粗了
            boolean hasFixColumn = fixColumnHeader.getChildCount() > 0;
            if(hasFixColumn){
                fixColumnHeader.getChildAt(0).setBackground(BackgroundDrawableCreater.getBorderDrawable(borderWidth, borderColorString, TRB, columnHeaderBackgroundColor));
            }
        }

    }

    //计算遮罩View的高度，视觉上要弥补缺失的border
    private int calculateLostBorderHeight(){
        View view = columnHeaderView_container.getChildAt(0);
         //可能的层级: view > LinearLayout > TextView结构下
         //可能的层级: view > 多个行LinearLayout > 每行一个或者多个LinearLayout作为cell > TextView
        if(view.getLayoutParams().height > 0){
            return  view.getLayoutParams().height;
        }else{
            ViewGroup viewGroup = (ViewGroup) view;
            //列名的行数
            int rowNumber = viewGroup.getChildCount();
            int sumHeight = 0;
            for(int i = 0; i < rowNumber; i++){
                ViewGroup rowView  = (ViewGroup) viewGroup.getChildAt(i);
                View firstCell = rowView.getChildAt(0);
                sumHeight += firstCell.getLayoutParams().height;
            }
            return sumHeight;
        }
    }

    /**
     * 自动调整控件的单元格大小
     */
    public void autoFitWidthAndHeight(){
           if(this.columnList == null){
               throw new RuntimeException("you must call method autoFitWidthAndHeight after tableview.setColumnDefine");
           }
        adjustCellWidth();
        adjustCellHeight();
        drawLostBorder();
        if(attachWindow){
            //fix bug
            //光是requestLayout并不够，各个子view都要requestLayout一下
            if(columnHeaderView_container.getChildCount() > 0){
                columnHeaderView_container.getChildAt(0).requestLayout();
            }
            fixColumnHeader.requestLayout();
            connerView.requestLayout();
            if(rowHeaderAndFixColoumCell_child.getChildCount() > 0){
                rowHeaderAndFixColoumCell_child.getChildAt(0).requestLayout();
            }
            if(cell_content.getChildCount() > 0){
                cell_content.getChildAt(0).requestLayout();
            }
            requestLayout();
            post(new Runnable() {
                @Override
                public void run() {
                    cell_content.syncScroll();
                }
            });
        }
    }

    /**
     * 作用就是每列宽度对齐
     */
    private void adjustCellWidth(){
        adjustRowHeaderWidth();
        allRealColumnfitRuleWidth();
        adjustCombiningColumnWidth();

    }

    private void adjustRowHeaderWidth(){
        if(tableData != null && tableData.getRowDataList() != null){
            List<T> rowData = tableData.getRowDataList();
            int final_width = 0;
            for(int i = 0; i < rowData.size(); i++){
                View rowHeaderCell = computeRowHeaderCell(i);
                //如果没有RowHeaderFactory的话,宽度应该是0
                int width = MeasureHelper.measureWrapWidth(rowHeaderCell);
                if(width > final_width){
                    final_width = width;
                }
            }

            for(int i = 0; i < rowData.size(); i++){
                View rowHeaderCell = computeRowHeaderCell(i);
                //仅仅保存一下，不需要setLayoutParams
                rowHeaderCell.getLayoutParams().width = final_width;

            }
            connerView.getLayoutParams().width = final_width;

        }
    }
    private View computeRowHeaderCell(int rowIndex){
        return ((ViewGroup) rowHeaderAndFixColoumCell_child.getChildAt(rowIndex)).getChildAt(0);
    }

    /**
     * 调整多级标题的宽度，横跨几列就设置成几列的宽的总和
     */
    private void adjustCombiningColumnWidth(){
        for(int i = 0; i < this.columnList.size(); i++){
            IColumn iColumn = this.columnList.get(i);
            if(iColumn instanceof  CombiningColumn){
                CombiningColumn combiningColumn = (CombiningColumn) iColumn;
                ViewGroup cell = (ViewGroup) computeHeaderCell(combiningColumn);
                int titleLevel_num = cell.getChildCount();
                ViewGroup real_column_row = (ViewGroup) cell.getChildAt(titleLevel_num - 1);
                for(int m = titleLevel_num - 2; m >= 0; m--){
                    List<CombiningColumn.LevelTitle> titles = combiningColumn.getColumnLevelTitle().get(m);
                    ViewGroup inner_row = (ViewGroup) cell.getChildAt(m);
                    for(int n = 0; n < titles.size(); n++){
                        CombiningColumn.LevelTitle tit = titles.get(n);
                        View tit_cell = inner_row.getChildAt(n);
                        tit_cell.getLayoutParams().width = computeTotalFixWidth(real_column_row, tit.getColumnIndexBegin(), tit.getColumnIndexEnd());

                    }
                }
            }
        }
    }


    /**
     * 调整view宽度到合法值
     * @param cell
     * @param minWidthDp
     * @param maxWidthDp
     */
    private void fitRuleWidth(View cell, int minWidthDp, int maxWidthDp){
         int minWidth = DensityTool.dip2px(getContext(), minWidthDp);
          int maxWidth = DensityTool.dip2px(getContext(), maxWidthDp);
          int measureWidth = MeasureHelper.measureWrapWidth(cell);
          if(measureWidth < minWidth){
              cell.getLayoutParams().width = minWidth;
          }else if(measureWidth > maxWidth){
              cell.getLayoutParams().width = maxWidth;
          }else{
              //exactly化
              cell.getLayoutParams().width = measureWidth;
          }
    }

    /**
     * 先设置，再取，一次布局后所有单元格宽高都是具体值
     * @param cell
     * @return
     */
    private int computeFixWidth(View cell){
           if(cell.getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT || cell.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT){
               throw new RuntimeException("this view not set fix width, but call computeFixWidth");
           }
           return cell.getLayoutParams().width;
    }

    private int computeTotalFixWidth(ViewGroup row, int beginIndex, int endIndex){
         int sum = 0;
         for(int i = beginIndex; i < endIndex; i++){
             sum += computeFixWidth(row.getChildAt(i));
         }
         return sum;
    }

    private void allRealColumnfitRuleWidth(){
        for(int i = 0; i < this.realColumnList.size(); i++){
            Column column = this.realColumnList.get(i);
            columnFitRuleWidth(column, i);

        }
    }
    private void columnFitRuleWidth(Column column, int columnIndex){
        if(tableData == null || tableData.getRowDataList() == null || tableData.getRowDataList().size() == 0){
            ViewGroup cell = (ViewGroup) computeHeaderCell(column, columnIndex);
            fitRuleWidth(cell, column.getMinWidthDp(), column.getMaxWidthDp());
        }else{
            ViewGroup header_cell = (ViewGroup) computeHeaderCell(column, columnIndex);
            List<T> rowsData = tableData.getRowDataList();
            int final_width = MeasureHelper.measureWrapWidth(header_cell);
            for(int i = 0; i < rowsData.size(); i++){

                View cell = computeContentCell(column, columnIndex, i);
                int cell_measureWidth = MeasureHelper.measureWrapWidth(cell);
                if(cell_measureWidth > final_width){
                    final_width = cell_measureWidth;
                }
            }
            if(final_width < DensityTool.dip2px(getContext(), column.getMinWidthDp())){
                final_width = DensityTool.dip2px(getContext(), column.getMinWidthDp());
            }else if(final_width > DensityTool.dip2px(getContext(), column.getMaxWidthDp())){
                final_width = DensityTool.dip2px(getContext(), column.getMaxWidthDp());
            }
            header_cell.getLayoutParams().width = final_width;

            for(int i = 0; i < rowsData.size(); i++){

                View cell = computeContentCell(column, columnIndex, i);
                cell.getLayoutParams().width = final_width;

            }
        }
    }

    /**
     * 查找对应的单元格
     * @param column
     * @return
     */
    private View computeHeaderCell(Column column, int columnIndex){
         if(column.isFix()){
               return fixColumnHeader.getChildAt(columnIndex);
         }else{
              if(column.getParentName() == null || column.getParentName().length == 0){
                    for(int i = 0; i < this.columnList.size(); i++){
                        if(column == this.columnList.get(i)){
                             return  columnHeaderView_container.getChildAt(i - fixColumnHeader.getChildCount());
                        }
                    }
                    throw new RuntimeException("impossible");
              }else{
                    CombiningColumn combiningColumn = computerFather(column);
                    ViewGroup combiningFatherView = computeHeaderCell(combiningColumn);
                    return  ((ViewGroup)(combiningFatherView.getChildAt(combiningFatherView.getChildCount() - 1))).getChildAt(computerIndexInFather(column));
              }
         }
    }

    private View computeHeaderCell(Column column){
        return computeHeaderCell(column, calculateIndexRealColumn(column));
    }

    private int calculateIndexRealColumn(Column column){
       return realColumnList.indexOf(column);
    }


    private ViewGroup computeTitleCellRow(CombiningColumn combiningColumn, int level){
          return (ViewGroup) computeHeaderCell(combiningColumn).getChildAt(level);
    }

    private ViewGroup computeTitleCellRow(Column column, int level){
           return  computeTitleCellRow(computerFather(column), level);
    }

    private ViewGroup computeTitleCell(CombiningColumn combiningColumn, CombiningColumn.LevelTitle title){

           for(int i = 0; i < combiningColumn.getColumnLevelTitle().size(); i++){
                  List<CombiningColumn.LevelTitle> list = combiningColumn.getColumnLevelTitle().get(i);
                  for(int j = 0; j < list.size(); j++){
                      if(title == list.get(j)){
                          return (ViewGroup) computeTitleCellRow(combiningColumn, i).getChildAt(j);
                      }
                  }
           }
           throw new RuntimeException("impossible");
    }

    private ViewGroup computeTitleCell(Column column, CombiningColumn.LevelTitle title){
        return computeTitleCell(computerFather(column), title);
    }

    private View computeContentCell(Column column, int columnIndex, int rowIndex){
             if(column.isFix()){
                 return  ((ViewGroup) rowHeaderAndFixColoumCell_child.getChildAt(rowIndex)).getChildAt(columnIndex + 1);
             }else{
                 return ((ViewGroup) cell_content.getChildAt(rowIndex)).getChildAt(columnIndex - fixColumnHeader.getChildCount());
             }
    }

    private ViewGroup computeHeaderCell(CombiningColumn combiningColumn){
         for(int i = 0; i < this.columnList.size(); i++){
             if(combiningColumn == this.columnList.get(i)){
                 return  (ViewGroup) columnHeaderView_container.getChildAt(i - fixColumnHeader.getChildCount());
             }
         }
         throw new RuntimeException("impossible");
    }

    private CombiningColumn computerFather(Column column){
        for(int i = 0; i < this.columnList.size(); i++){
             if(this.columnList.get(i) instanceof CombiningColumn){
                 CombiningColumn combiningColumn = (CombiningColumn) this.columnList.get(i);
                 for(int j = 0; j < combiningColumn.getColumnList().size(); j++){
                     if(column == combiningColumn.getColumnList().get(j)){
                         return  combiningColumn;
                     }
                 }
             }
        }
        return  null;
    }
    private int computerIndexInFather(Column column){
        for(int i = 0; i < this.columnList.size(); i++){
            if(this.columnList.get(i) instanceof CombiningColumn){
                CombiningColumn combiningColumn = (CombiningColumn) this.columnList.get(i);
                for(int j = 0; j < combiningColumn.getColumnList().size(); j++){
                    if(column == combiningColumn.getColumnList().get(j)){
                        return  j;
                    }
                }
            }
        }
        throw new RuntimeException("impossible");
    }

    /**
     * 作用就是每行高度对齐
     */
    private void adjustCellHeight(){
        adjustContentRowsCellHeight();
        adjustColumnHeaderHeight();
    }

    private void adjustContentRowsCellHeight(){
        if(tableData != null && tableData.getRowDataList() != null){
             for(int i = 0; i < tableData.getRowDataList().size(); i++){
                 adjustContentRowCellHeight(i);
             }
        }
    }

    private void adjustContentRowCellHeight(int rowIndex){
         List<View> list = getRowAllCell(rowIndex);
         int final_height = 0;
         for(int i = 0; i < list.size(); i++){
             View cell = list.get(i);
             if(BuildConfig.DEBUG){
                 boolean error = cell.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT || cell.getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT;
                 if(error){
                     throw new RuntimeException("impossible");
                 }
             }
             int measureHeight = MeasureHelper.measureWrapHeight(cell, cell.getLayoutParams().width);
             if(measureHeight > final_height){
                 final_height = measureHeight;
             }
         }
        if(final_height < DensityTool.dip2px(getContext(), minCellHeightDp)){
            final_height = DensityTool.dip2px(getContext(), minCellHeightDp);
        }
        for(int i = 0; i < list.size(); i++){
            list.get(i).getLayoutParams().height = final_height;
        }
    }

    private List<View> getRowAllCell(int rowIndex){
          List<View> list = new ArrayList<View>();
          ViewGroup rowHeaderAndFixColumn = (ViewGroup) rowHeaderAndFixColoumCell_child.getChildAt(rowIndex);
           for(int i = 0; i < rowHeaderAndFixColumn.getChildCount(); i++){
               list.add(rowHeaderAndFixColumn.getChildAt(i));
           }
          ViewGroup contentRow = (ViewGroup) cell_content.getChildAt(rowIndex);
          for(int i = 0; i < contentRow.getChildCount(); i++){
              list.add(contentRow.getChildAt(i));
          }
        return  list;
    }

    private void adjustColumnHeaderHeight(){
         int maxHeight_normalColumn = calculateNormalColumnMaxHeight();
        List<Map<String, Object>>  combiningColumnHeight = calculateCombiningHeight();
         int maxHeight_CombiningColumn  = calculateCombiningColumnMaxHeight(combiningColumnHeight);
        int final_height = Math.max(maxHeight_normalColumn, maxHeight_CombiningColumn);
         if(final_height < DensityTool.dip2px(getContext(), minCellHeightDp)){
             final_height = DensityTool.dip2px(getContext(), minCellHeightDp);
         }
        stretchLevelTitle(final_height, combiningColumnHeight);
        stretchNormalColumnHeader(final_height);
        stretchConnerViewHeight(final_height);

    }
     private void stretchConnerViewHeight(int final_height){
           connerView.getLayoutParams().height = final_height;

     }
    private void stretchLevelTitle(int finalHeight, List<Map<String, Object>>  combiningColumnHeight){
           for(Map<String, Object> map : combiningColumnHeight){
                CombiningColumn column = (CombiningColumn) map.get("column");
                int[] heightArray = (int[]) map.get("height");
                int sum = sum(heightArray);
                int plus = (finalHeight - sum) / heightArray.length;
                int remainder = (finalHeight - sum) % heightArray.length;
               for(int n = 0; n < heightArray.length; n++){
                   heightArray[n] += plus;
                    if(n == heightArray.length - 1){
                        heightArray[n] += remainder;
                    }
               }
                ViewGroup cell = computeHeaderCell(column);
                stretchCombiningHeader(cell, heightArray);
           }
    }
    private void stretchCombiningHeader(ViewGroup combiningCell, int[] heightArray){
         if(combiningCell.getChildCount() != heightArray.length){
             throw new RuntimeException("impossible");
         }
        for(int i = 0; i < combiningCell.getChildCount(); i++){
            ViewGroup row = (ViewGroup) combiningCell.getChildAt(i);
            for(int j = 0; j < row.getChildCount(); j++){
                View little_cell = row.getChildAt(j);
                little_cell.getLayoutParams().height = heightArray[i];

            }
        }
    }
    private void stretchNormalColumnHeader(int finalHeight){
          for(int i = 0; i < columnList.size(); i++){
              IColumn iColumn = columnList.get(i);
              if(iColumn instanceof  Column){
                  View cell = computeHeaderCell((Column) iColumn);
                  cell.getLayoutParams().height = finalHeight;

              }
          }
    }
    private int calculateNormalColumnMaxHeight(){
        int maxHeight = 0;
        for(int i = 0; i < this.columnList.size(); i++){
            IColumn iColumn = this.columnList.get(i);
            if(iColumn instanceof Column){
                View view = computeHeaderCell((Column) iColumn);
                if(BuildConfig.DEBUG){
                    int paramsWidth = view.getLayoutParams().width;
                    boolean error = paramsWidth == ViewGroup.LayoutParams.MATCH_PARENT || paramsWidth == ViewGroup.LayoutParams.WRAP_CONTENT;
                    if(error){
                        throw new RuntimeException("");
                    }
                }
                int measureHeight = MeasureHelper.measureWrapHeight(view, view.getLayoutParams().width);
                if(measureHeight > maxHeight){
                    maxHeight = measureHeight;
                }
            }
        }
        return maxHeight;
    }

    /**
     * 注意：会修改入参，详见alignVerticalLevel方法，会把每层的高度对齐并且修改成一致
     * @param combiningColumnHeight
     * @return
     */
    private int calculateCombiningColumnMaxHeight(List<Map<String, Object>>  combiningColumnHeight){
        alignVerticalLevel(combiningColumnHeight);
        int maxHeight = 0;
        for(int i = 0; i < combiningColumnHeight.size(); i++){
            Map<String, Object> map = combiningColumnHeight.get(i);
            int[] heightArray = (int[]) map.get("height");
            int sum = sum(heightArray);
            if(sum > maxHeight){
                maxHeight = sum;
            }
        }
        return  maxHeight;
    }

    private int sum(int[] a){
        int sum = 0;
        for(int i : a){
            sum += i;
        }
        return sum;
    }

    private List<Map<String, Object>> calculateCombiningHeight(){
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for(int i = 0; i < this.columnList.size(); i++){
            IColumn iColumn = this.columnList.get(i);
            if(iColumn instanceof CombiningColumn){
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("column", iColumn);
                map.put("height",calculateOneCombiningHeight((CombiningColumn) iColumn));
                list.add(map);
            }
        }
        return list;

    }

    private int[] calculateOneCombiningHeight(CombiningColumn column){
        int[] h = new int[column.getColumnLevelTitle().size() + 1];
        ViewGroup cell = computeHeaderCell(column);
         if(h.length != cell.getChildCount()){
             throw new RuntimeException("impossible");
         }
         for(int i = 0; i < h.length; i++){
               ViewGroup inner_row = (ViewGroup) cell.getChildAt(i);
                int maxHeight = 0;
                for(int j = 0; j < inner_row.getChildCount(); j++){
                    View little_cell = inner_row.getChildAt(j);
                    if(BuildConfig.DEBUG){
                        int little_cell_paramsWidth = little_cell.getLayoutParams().width;
                        boolean error = little_cell_paramsWidth == ViewGroup.LayoutParams.WRAP_CONTENT || little_cell_paramsWidth == ViewGroup.LayoutParams.MATCH_PARENT;
                        if(error){
                            throw new RuntimeException("impossible");
                        }
                    }
                    int measureHeight = MeasureHelper.measureWrapHeight(little_cell, little_cell.getLayoutParams().width);
                    if(measureHeight > maxHeight){
                        maxHeight = measureHeight;
                    }
                }
             h[i] = maxHeight;
         }
        return h;

    }

    /**
     * 表格可能存在多个组合列 ，如果分层数量一样。 那么对齐这些层。 比如:一个组合列有3层， 另外一个组合列也是3层。就可以对齐
     * @param list
     */
    private void alignVerticalLevel(List<Map<String, Object>> list){
           ArrayList<Integer> done = new ArrayList<Integer>();
           HashMap<Integer, List<int[]>> map = new HashMap<Integer, List<int[]>>();
           for(int i = 0; i < list.size(); i++){
                 int[] height = (int[]) list.get(i).get("height");
                  if(done.contains(height.length)){
                      map.get(height.length).add(height);
                  }else{
                      done.add(height.length);
                      map.put(height.length, new ArrayList<int[]>());
                      map.get(height.length).add(height);
                  }
           }
           for(int i = 0; i < done.size(); i++){
               changeValueForAlignVerticalLevel(map.get(done.get(i)));
           }


    }

    private void changeValueForAlignVerticalLevel(List<int[]> list){
         int level = list.get(0).length;
          for(int i = 0; i < level; i++){
               int maxHeight = DensityTool.dip2px(getContext(), minCellHeightDp);
              for(int j = 0; j < list.size(); j++){
                  int[] heightArray = list.get(j);
                  if(heightArray[i] > maxHeight){
                      maxHeight = heightArray[i];
                  }
              }
              for(int j = 0; j < list.size(); j++){
                  list.get(j)[i] = maxHeight;
              }
          }

    }
    /**
     * 是否是最后一列
     * @param combiningColumn
     * @return
     */
    private boolean isLastColumn(CombiningColumn combiningColumn){
        Column column = combiningColumn.getColumnList().get(combiningColumn.getColumnList().size() - 1);
        return  column == realColumnList.get(realColumnList.size() - 1);
    }

    /**
     * 是否是最后一列
     * @param column
     * @return
     */
    private boolean isLastColumn(Column column){
        return  column == realColumnList.get(realColumnList.size() - 1);
    }

    private  LinearLayout createOneColumnHeaderCellView(Column column){
           String borderLTRB = TRB;

            LinearLayout cell = createOneCellView(borderLTRB, columnHeaderBackgroundColor);
           TextView textView = new TextView(getContext());
            textView.setText(column.getColumnName());
            textView.setTextSize(textSizeDp);
            if(!TextUtils.isEmpty(columnHeaderTextColor)){
                 textView.setTextColor(Color.parseColor(columnHeaderTextColor));
             }
             cell.addView(textView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return cell;
    }

    private LinearLayout createOneColumnHeaderCellView(CombiningColumn combiningColumn){
        LinearLayout linearLayout = createOneRowLinearLayout(VERTICAL);
        //增加多级标题
        for(int i = 0; i < combiningColumn.getColumnLevelTitle().size(); i++){
            List<CombiningColumn.LevelTitle> levelTitleList = combiningColumn.getColumnLevelTitle().get(i);
            LinearLayout row = createOneRowLinearLayout(HORIZONTAL);
            linearLayout.addView(row);
            for(int m = 0; m < levelTitleList.size(); m++){
                CombiningColumn.LevelTitle levelTitle = levelTitleList.get(m);
                String rtlb = RB;
                if(i == 0){
                    //顶层需要有top border，否则当设置了columnBackgroundColor的时候，组合列top border会缺失
                    rtlb = TRB;
                }
                row.addView(createLevelTitleCell(levelTitle.getName(), combiningColumn.getColumnLevelTitle().size(), rtlb));
            }

        }
        //增加最底层的子列名
        LinearLayout row = createOneRowLinearLayout(HORIZONTAL);
        linearLayout.addView(row);
        List<Column> columns = combiningColumn.getColumnList();
        for(int i = 0; i < columns.size(); i++){
            Column column = columns.get(i);
           row.addView(createLevelTitleCell(column.getColumnName(), combiningColumn.getColumnLevelTitle().size(), RB));
        }
        return linearLayout;
    }



    /**
     * 创建多级列名其中一个单元格
     * @param name
     * @param totalLevelNum 总共多少级父列名，级数多的话相应调整padding和字体大小
     * @return
     */
    private LinearLayout createLevelTitleCell(String name, int totalLevelNum,  String borderLTRB){
            int cellHorizontalPaddin;
            int cellVerticalPaddin;
            int textSize;
            if(totalLevelNum <= 1){
                cellHorizontalPaddin = secondHorizontalPaddingDp;
                cellVerticalPaddin = secondVerticalPaddingDp;
                textSize = secondTextSizeDp;
            }else{
                 cellHorizontalPaddin = thirdHorizontalPaddingDp;
                 cellVerticalPaddin = thirdVerticalPaddingDp;
                 textSize = thirdTextSizeDp;
            }

            LinearLayout cell = createOneCellView(borderLTRB, columnHeaderBackgroundColor, cellHorizontalPaddin, cellVerticalPaddin);
           TextView textView = new TextView(getContext());
           textView.setText(name);
           textView.setTextSize(textSize);
           if(!TextUtils.isEmpty(columnHeaderTextColor)){
              textView.setTextColor(Color.parseColor(columnHeaderTextColor));
           }
            cell.addView(textView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
           return cell;
    }

    /**
     * 创建rowHeader
     * @param rowIndex
     * @return
     */
    private LinearLayout createRowHeaderCell(int rowIndex){
        LinearLayout cell = createOneCellView(LRB, rowHeaderBackgroundColor);
        if(rowHeaderViewFactory != null){
            cell.addView(rowHeaderViewFactory.create(tableData, rowIndex, tableData.getRowDataList().get(rowIndex)));
        }else{
            cell.setPadding(0, 0, 0, 0);
        }
        return cell;
    }

    /**
     * 创建普通的内容单元格
     * @return
     */
    private LinearLayout createContentCell(int rowIndex, int columnIndex){
        LinearLayout cell = createOneCellView(RB, cellBackgroundColor);
        TextView textView = new TextView(getContext());
        textView.setTextSize(textSizeDp);
        textView.setTextColor(Color.parseColor(cellTextColor));
        cell.addView(textView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        T dataItem = tableData.getRowDataList().get(rowIndex);
        String property = realColumnList.get(columnIndex).getProperty();
        Object value = null;
        if(dataItem instanceof Map){
            value = ((Map) dataItem).get(property);
        }else{
            value = ReflectionUtil.getFieldValue(dataItem, property);
        }
        if(value != null){
            textView.setText(value.toString());
        }
        return cell;
    }

    /**
     * 设置数据源
     * @param tableData
     */
     public void loadData(TableData<T> tableData){
          if(tableData == null || tableData.getRowDataList() == null){
              throw new IllegalArgumentException("table data is null");
          }
          this.tableData = tableData;
          clearRowView();
          addRowViews();
          autoFitWidthAndHeight();

     }

    /**
     * 清除行数据
     */
    private void clearRowView(){
        rowHeaderAndFixColoumCell_child.removeAllViews();
        cell_content.removeAllViews();
     }
     private void addRowViews(){
        for(int i = 0; i < this.tableData.getRowDataList().size(); i++){
               addRowView(i);
        }
     }
     private void addRowView(int rowIndex){
          LinearLayout row_header =  createOneRowLinearLayout(HORIZONTAL);
          row_header.addView(createRowHeaderCell(rowIndex));
          LinearLayout content_row = createOneRowLinearLayout(HORIZONTAL);
          for(int i = 0; i < this.realColumnList.size(); i++){
               Column column = this.realColumnList.get(i);
               if(column.isFix()){
                   row_header.addView(createContentCell(rowIndex, i));
               }else{
                   content_row.addView(createContentCell(rowIndex, i));
               }
          }
         cell_content.addView(content_row, rowIndex);
         rowHeaderAndFixColoumCell_child.addView(row_header, rowIndex);
     }

     private void deleteRowView(int rowIndex){
         cell_content.removeViewAt(rowIndex);
         rowHeaderAndFixColoumCell_child.removeViewAt(rowIndex);
     }



    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachWindow = true;
        if(columnList == null || columnList.size() == 0){
            throw new RuntimeException("tableview must set columns before insert Layout");
        }
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attachWindow = false;

    }

    private LinearLayout createOneCellView(String borderLTRB, String backgroundColor){
         LinearLayout cell_container = new LinearLayout(getContext());
         cell_container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
         cell_container.setGravity(Gravity.CENTER);
         cell_container.setOrientation(VERTICAL);
         cell_container.setBackground(BackgroundDrawableCreater.getBorderDrawable(borderWidth, borderColorString, borderLTRB, backgroundColor));
         cell_container.setPadding(DensityTool.dip2px(getContext(),cellHorizontalPaddingDp), DensityTool.dip2px(getContext(), cellVerticalPaddingDp), DensityTool.dip2px(getContext(),cellHorizontalPaddingDp), DensityTool.dip2px(getContext(), cellVerticalPaddingDp));
         return  cell_container;

    }

    /**
     *
     * @param borderLTRB
     * @param backgroundColor
     * @param cellHorizontalPadding 单位是dp
     * @param cellVerticalPadding 单位是dp
     * @return
     */
    private LinearLayout createOneCellView(String borderLTRB, String backgroundColor, int cellHorizontalPadding, int cellVerticalPadding){
        LinearLayout cell_container = new LinearLayout(getContext());
        cell_container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        cell_container.setGravity(Gravity.CENTER);
        cell_container.setOrientation(VERTICAL);
        cell_container.setPadding(DensityTool.dip2px(getContext(), cellHorizontalPadding),
                                   DensityTool.dip2px(getContext(), cellVerticalPadding),
                                   DensityTool.dip2px(getContext(), cellHorizontalPadding),
                                   DensityTool.dip2px(getContext(), cellVerticalPadding)
                                   );
        cell_container.setBackground(BackgroundDrawableCreater.getBorderDrawable(borderWidth, borderColorString, borderLTRB, backgroundColor));
        return  cell_container;

    }

    private LinearLayout createOneRowLinearLayout(int orientation){
        LinearLayout row_linear = new LinearLayout(getContext());
        row_linear.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row_linear.setOrientation(orientation);
        return  row_linear;
    }


    public interface RowHeaderViewFactory<T>{
        public View create(TableData<T> tableData, int rowIndex, T itemData);
    }


    public void insertRow(int rowIndex, T data){
         if(tableData == null){
              if(rowIndex == 0){
                  List<T> dataList = new ArrayList<T>();
                  dataList.add(data);
                  loadData(new TableData<T>(dataList));
              }else{
                  throw new IllegalArgumentException("");
              }
         }else{
             tableData.getRowDataList().add(rowIndex, data);
             addRowView(rowIndex);
             autoFitWidthAndHeight();
         }

    }

    public void notifyDataChange(int rowIndex){
        T data = tableData.getRowDataList().remove(rowIndex);
        deleteRowView(rowIndex);
        insertRow(rowIndex, data);
    }

    public T deleteRow(int rowIndex){
          T data = tableData.getRowDataList().remove(rowIndex);
           deleteRowView(rowIndex);
           post(new Runnable() {
               @Override
               public void run() {
                   cell_content.syncScroll();
               }
           });

           return data;
    }





  //=================================================下面是getter，setter==============================================================================

    public int getSecondHorizontalPadding() {
        return secondHorizontalPaddingDp;
    }

    public void setSecondHorizontalPadding(int secondHorriblePadding) {
        this.secondHorizontalPaddingDp = secondHorriblePadding;
    }

    public int getSecondVerticalPadding() {
        return secondVerticalPaddingDp;
    }

    public void setSecondVerticalPadding(int secondVerticalPadding) {
        this.secondVerticalPaddingDp = secondVerticalPadding;
    }

    public int getThirdHorizontalPadding() {
        return thirdHorizontalPaddingDp;
    }

    public void setThirdHorizontalPadding(int thirdHorriblePadding) {
        this.thirdHorizontalPaddingDp = thirdHorriblePadding;
    }

    public int getThirdVerticalPadding() {
        return thirdVerticalPaddingDp;
    }

    public void setThirdVerticalPadding(int thirdVerticalPadding) {
        this.thirdVerticalPaddingDp = thirdVerticalPadding;
    }


    public int getCellHorizontalPadding() {
        return cellHorizontalPaddingDp;
    }

    public void setCellHorizontalPadding(int cellHorriblePadding) {
        this.cellHorizontalPaddingDp = cellHorriblePadding;
    }

    public int getCellVerticalPadding() {
        return cellVerticalPaddingDp;
    }

    public void setCellVerticalPadding(int cellVerticalPadding) {
        this.cellVerticalPaddingDp = cellVerticalPadding;
    }

    public String getBorderColorString() {
        return borderColorString;
    }

    public void setBorderColorString(String borderColorString) {
        this.borderColorString = borderColorString;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    public String getCellBackgroundColor() {
        return cellBackgroundColor;
    }

    public void setCellBackgroundColor(String cellBackgroundColor) {
        this.cellBackgroundColor = cellBackgroundColor;
    }

    public String getColumnHeaderBackgroundColor() {
        return columnHeaderBackgroundColor;
    }

    public void setColumnHeaderBackgroundColor(String columnHeaderBackgroundColor) {
        this.columnHeaderBackgroundColor = columnHeaderBackgroundColor;
    }

    public String getRowHeaderBackgroundColor() {
        return rowHeaderBackgroundColor;
    }

    public void setRowHeaderBackgroundColor(String rowHeaderBackgroundColor) {
        this.rowHeaderBackgroundColor = rowHeaderBackgroundColor;
    }

    public String getCellTextColor() {
        return cellTextColor;
    }

    public void setCellTextColor(String cellTextColor) {
        this.cellTextColor = cellTextColor;
    }

    public String getColumnHeaderTextColor() {
        return columnHeaderTextColor;
    }

    public void setColumnHeaderTextColor(String columnHeaderTextColor) {
        this.columnHeaderTextColor = columnHeaderTextColor;
    }



    public int getTextSizeDp() {
        return textSizeDp;
    }

    public void setTextSizeDp(int textSizeDp) {
        this.textSizeDp = textSizeDp;
    }

    public int getSecondTextSizeDp() {
        return secondTextSizeDp;
    }

    public void setSecondTextSizeDp(int secondTextSizeDp) {
        this.secondTextSizeDp = secondTextSizeDp;
    }

    public int getThirdTextSizeDp() {
        return thirdTextSizeDp;
    }

    public void setThirdTextSizeDp(int thirdTextSizeDp) {
        this.thirdTextSizeDp = thirdTextSizeDp;
    }

    public RowHeaderViewFactory getRowHeaderViewFactory() {
        return rowHeaderViewFactory;
    }

    public void setRowHeaderViewFactory(RowHeaderViewFactory rowHeaderViewFactory) {
        this.rowHeaderViewFactory = rowHeaderViewFactory;
    }

    public TableData<T> getTableData() {
        return tableData;
    }

    public void setTableData(TableData<T> tableData) {
        this.tableData = tableData;
    }
}
