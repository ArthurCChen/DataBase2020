package cn.edu.thssdb.schema;
//一个用户的使用成为一个catalog
public class Catalog {
    private Database curDatabase;
    private String curDatabasename;
    private String username;

    public Catalog(String username){
        this.username = username;
        this.curDatabasename = Manager.getInstance().getDefaultDatabaseName();
        this.curDatabase = Manager.getInstance().getDatabase(curDatabasename);
    }

    public void switchDatabase(String databaseName){
        this.curDatabasename = databaseName;
        this.curDatabase = Manager.getInstance().switchDatabase(curDatabasename);
    }

    public void addDatabase(String databasename){
        //TODO
    }

    public void deleteDatabase(String databaseName){
        //TODO
    }

    public void disconnect(){
        //TODO
    }

}
