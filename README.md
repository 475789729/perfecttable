       演示图片:<br>
        ![Image text](https://github.com/475789729/perfecttable/blob/master/test.gif)

       特性:
       1.表格过宽或者过高之后能够产生可滚动效果
       2.第一行作为列名不上下滚动，第一列作为行头，不左右滚动
       3.支持其他列设置为固定列，设置之后该列不再左右滚动
       4.支持多级列名，如果上级列名相等，就自动组合
       5.支持第一列的view自定义
       6.单元格颜色，padding，border等的设置
       
       
       
       使用方法: 
        Column column1 = new Column();
        column1.setColumnName("姓名");
        column1.setProperty("name");

        Column column2 = new Column();
        column2.setColumnName("性别");
        column2.setProperty("sex");

        //固定列自动提前位置
        Column column3 = new Column();
        column3.setColumnName("固定列，不左右滚动");
        column3.setProperty("height");
        column3.setFix(true);

        Column column4 = new Column();
        column4.setColumnName("子列一");
        column4.setParenetName(new String[]{"组合列名"});
        column4.setProperty("sd");



        Column column5 = new Column();
        column5.setColumnName("子列二");
        //控件会自动组合
        column5.setParenetName(new String[]{"组合列名"});
        column5.setProperty("sdd");

        List<Column> columnList = new ArrayList<Column>();
         columnList.add(column1);
         columnList.add(column2);
         columnList.add(column3);
         columnList.add(column4);
         columnList.add(column5);
        PerfectTableViewBuilder builder = new PerfectTableViewBuilder(this);
         builder.setColumnList(columnList);
         //行头的view是自定义的，不需要的话，不设置RowHeaderViewFactory就行
         builder.setRowHeaderViewFactory(new PerfectTableView.RowHeaderViewFactory() {
             @Override
             public View create(TableData tableData, int rowIndex, Object itemData) {
                 TextView tv = new TextView(MainActivity.this);
                 tv.setText(String.valueOf(rowIndex));
                 tv.setTextSize(16);
                 return tv;
             }
         });
         PerfectTableView<Student> tableView = builder.create();
         List<Student> studentList = new ArrayList<Student>();
         studentList.add(new Student("张三", "男", "170", "sd", "sdd"));
        studentList.add(new Student("李四", "男", "170", "sd", "sdd"));
        tableView.loadData(new TableData<Student>(studentList));
        ((RelativeLayout) findViewById(R.id.content)).addView(tableView);
