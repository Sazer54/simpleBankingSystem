package SImple_Banking_System_Unbound;

public class Model {

    private final String DEFAULT_DB_NAME = "bankingSystem.db";
    private final String DEFAULT_DB_PATH_ROOT = "jdbc:sqlite:";
    private String dbName;
    private final String INN = "400000";
    private String pathToDataBase;
    private Integer currentAccountIndex;
    private int optionSelection;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDefaultDbName() {
        return DEFAULT_DB_NAME;
    }

    public String getDefaultDbPathRoot() {
        return DEFAULT_DB_PATH_ROOT;
    }

    public String getPathToDataBase() {
        return pathToDataBase;
    }

    public void setPathToDataBase(String pathToDataBase) {
        this.pathToDataBase = pathToDataBase;
    }

    public String getINN() {
        return INN;
    }

    public int getOptionSelection() {
        return optionSelection;
    }

    public void setOptionSelection(int optionSelection) {
        this.optionSelection = optionSelection;
    }

    public Integer getCurrentAccountIndex() {
        return currentAccountIndex;
    }

    public void setCurrentAccountIndex(Integer currentAccountIndex) {
        this.currentAccountIndex = currentAccountIndex;
    }
}
