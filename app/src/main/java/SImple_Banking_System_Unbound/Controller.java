package SImple_Banking_System_Unbound;

import org.sqlite.SQLiteDataSource;

import javax.xml.transform.Result;
import java.io.File;
import java.sql.*;
import java.util.Random;
import java.util.Scanner;
import java.util.zip.CheckedOutputStream;

public class Controller {

    private View view;
    private Model model;

    public Controller() {
        this.model = new Model();
        this.view = new View();
    }

    public void work(String[] args) {
        model.setDbName(model.getDefaultDbName());
        for (int i = 0; i < args.length; i++) {
            try {
                if (args[i].equals("-fileName")) {
                    model.setDbName(args[i+1]);
                    i++;
                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Wrong arguments passed");
            }
        }
        model.setPathToDataBase(model.getDefaultDbPathRoot() + model.getDbName());
        this.createDatabase();
        do {
            this.promptSelection();
            this.operateBasedOnSelection();
        }
        while (model.getOptionSelection() != 0);
    }

    private void createDatabase() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(model.getPathToDataBase());
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                String queryText = "CREATE TABLE IF NOT EXISTS card(id INTEGER PRIMARY KEY, number TEXT, pin TEXT, balance INTEGER DEFAULT 0);";
                statement.executeUpdate(queryText);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void promptSelection() {
        if (model.getCurrentAccountIndex() == null) {
            view.printLoggedOutMenu();
        } else {
            view.printLoggedInMenu();
        }
        Scanner scanner = new Scanner(System.in);
        model.setOptionSelection(scanner.nextInt());
    }

    private void operateBasedOnSelection() {
        if (model.getCurrentAccountIndex() == null) {
            switch (model.getOptionSelection()) {
                case 1:
                    Account newAccount = this.generateNewAccount();
                    view.printAccountDetails(newAccount);
                    this.addAccount(newAccount);
                    break;
                case 2:
                    this.logIn();
                    break;
                case 0:
                    view.printByeMessage();
                    break;
                default:
                    view.printWrongOptionSelection();
            }
        }
        else {
            switch (model.getOptionSelection()) {
                case 1:
                    view.printBalance(this.getBalanceById(model.getCurrentAccountIndex()));
                    break;
                case 2:
                    this.addIncome();
                    break;
                case 3:
                    this.transfer();
                    break;
                case 4:
                    this.closeAccount();
                    break;
                case 5:
                    this.logOut();
                    break;
                case 0:
                    view.printByeMessage();
                    break;
                default:
                    view.printWrongOptionSelection();
            }
        }
    }

    private Account generateNewAccount() {
        Random random = new Random();
        String accountId = this.appendZeroes(String.valueOf(random.nextInt(1_000_000_000)), 9);
        String accountPin = this.appendZeroes(String.valueOf(random.nextInt(10000)), 4);
        String checkSum = this.getCheckSum(model.getINN() + accountId);
        String accountCardNumber = model.getINN() + accountId + checkSum;
        return new Account(accountCardNumber, accountPin);
    }

    private void addAccount(Account account) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(model.getPathToDataBase());

        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                String queryText = "INSERT INTO card(number, pin) VALUES('"+account.getCardNumber() + "', '" + account.getPin() + "')";
                statement.executeUpdate(queryText);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private String appendZeroes(String string, int length) {
        StringBuilder stringBuilder = new StringBuilder(string);
        while (stringBuilder.length() != length) {
            stringBuilder.insert(0, "0");
        }
        return stringBuilder.toString();
    }

    private String getCheckSum(String cardNumber) {
        int digitsSum = 0;
        String checkSum;
        int currentNumber;

        for (int i = 0; i < cardNumber.length(); i++) {
            currentNumber = Character.getNumericValue(cardNumber.charAt(i));
            currentNumber *= i % 2 == 0 ? 2 : 1;
            currentNumber -= currentNumber > 9 ? 9 : 0;
            digitsSum += currentNumber;
        }
        checkSum = String.valueOf((10 - (digitsSum % 10)) % 10);
        return checkSum;
    }

    private void logIn() {
        Scanner scanner = new Scanner(System.in);
        view.promptCardNumber();
        String inputCardNumber = scanner.nextLine();
        view.promptPin();
        String inputPin = scanner.nextLine();

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(model.getPathToDataBase());
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                String queryText = "SELECT id, number, pin from card;";
                try (ResultSet credentials = statement.executeQuery(queryText)) {
                    while (credentials.next()) {
                        Integer id = credentials.getInt("id");
                        String cardNumber = credentials.getString("number");
                        String pin = credentials.getString("pin");

                        if (inputCardNumber.equals(cardNumber) && inputPin.equals(pin)) {
                            model.setCurrentAccountIndex(id);
                            view.printSuccessfulLogin();
                            return;
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        view.printLoginError();
    }

    private void logOut() {
        model.setCurrentAccountIndex(null);
        view.printLoggedOutMessage();
    }

    private void closeAccount() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(model.getPathToDataBase());
        try (Connection connection = dataSource.getConnection()) {
            String deleteAccount = "DELETE FROM card WHERE id = " + model.getCurrentAccountIndex();
            try (Statement deleteStatement = connection.createStatement()) {
                deleteStatement.executeUpdate(deleteAccount);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        model.setCurrentAccountIndex(null);
        view.printClosedAccountMessage();
    }

    private void addIncome() {
        Scanner scanner = new Scanner(System.in);
        view.promptAddedIncome();
        int addedIncome = scanner.nextInt();
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(model.getPathToDataBase());
        try (Connection connection = dataSource.getConnection()) {
            String updateBalance = "UPDATE card set balance = balance + " + addedIncome + " where id = " + model.getCurrentAccountIndex();
            try (Statement addIncomeStatement = connection.createStatement()) {
                addIncomeStatement.executeUpdate(updateBalance);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void transfer() {
        view.announceTransfer();
        view.promptTransferCardNumber();
        Scanner scanner = new Scanner(System.in);
        String numberToSendTo = scanner.nextLine();
        if (!validateCardNumber(numberToSendTo)) {
            view.invalidCardNumber();
            return;
        }
        if (numberToSendTo.equals(this.getCardNumberById(model.getCurrentAccountIndex()))) {
            view.sameAccountTransfer();
            return;
        }

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(model.getPathToDataBase());
        try (Connection connection = dataSource.getConnection()) {
            String selectCard = "SELECT * FROM card where number = ?";
            try (PreparedStatement selectCardStatement = connection.prepareStatement(selectCard)) {
                selectCardStatement.setString(1, numberToSendTo);
                ResultSet resultSet = selectCardStatement.executeQuery();
                if (!resultSet.next()) {
                    view.noSuchCard();
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        view.promptTransferAmount();
        int transferAmount = scanner.nextInt();
        if (this.getBalanceById(model.getCurrentAccountIndex()) < transferAmount) {
            view.notEnoughMoney();
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            String updateBalance = "UPDATE card set balance = balance + ? where id = ?";
            try (PreparedStatement updateBalanceStatement = connection.prepareStatement(updateBalance)) {
                updateBalanceStatement.setInt(1, transferAmount * -1);
                updateBalanceStatement.setInt(2, model.getCurrentAccountIndex());
                updateBalanceStatement.executeUpdate();

                updateBalanceStatement.setInt(1, transferAmount);
                updateBalanceStatement.setInt(2, this.getIdByCardNumber(numberToSendTo));
                updateBalanceStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean validateCardNumber(String cardNumber) {
        return String.valueOf(cardNumber.charAt(cardNumber.length() - 1)).equals(this.getCheckSum(cardNumber.substring(0, cardNumber.length()-1)));
    }

    private Integer getIdByCardNumber(String cardNumber) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(model.getPathToDataBase());
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                String queryText = "SELECT id FROM card WHERE number = " + cardNumber;
                try (ResultSet resultSet = statement.executeQuery(queryText)) {
                    return resultSet.getInt(1);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getCardNumberById(Integer id) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(model.getPathToDataBase());
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                String queryText = "SELECT number FROM card WHERE id = " + id;
                try (ResultSet resultSet = statement.executeQuery(queryText)) {
                    return resultSet.getString(1);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getBalanceById(Integer id) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(model.getPathToDataBase());
        try (Connection connection = dataSource.getConnection()) {
            String selectBalance = "SELECT balance from card where id = " + id;
            try (Statement selectBalanceStatement = connection.createStatement()) {
                try (ResultSet balance = selectBalanceStatement.executeQuery(selectBalance)) {
                    return balance.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
