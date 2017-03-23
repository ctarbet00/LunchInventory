package LunchInventory;

/**
 *
 * @author Cole Tarbet
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SQLConnector {

    private Connection conn = null;
    private PreparedStatement preparedStatement = null;

    /**
     *
     * @param ip_address
     * @param database
     * @param user
     * @param password
     *
     * The constructor attempts to connect to the database specified
     */
    SQLConnector(String ip_address, String database, String user, String password) {
        try {
            //load the driver
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection("jdbc:mysql://" + ip_address + "/" + database, user, password);
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not loaded: " + e.getMessage());
            //System.exit(1);
        } catch (SQLException e) {
            System.out.println("Driver not loaded: " + e.getMessage());
            //System.exit(2);
        } catch (InstantiationException e) {
            System.out.println("Driver not loaded: " + e.getMessage());
            //System.exit(2);
        } catch (IllegalAccessException e) {
            System.out.println("Driver not loaded: " + e.getMessage());
            //System.exit(2);
        }
    }

    /**
     * Creates database tables required for the application.
     ******************************************************************************************* 
        use inventory;

        DROP TABLE items;

        DROP TABLE category;

        CREATE TABLE category (	id int(64) NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                                        categoryName varchar(255) NOT NULL, 
                                                        UNIQUE (id));

        CREATE TABLE items (id int(64) NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                                hsqty int(64) NOT NULL DEFAULT 0,
                                                elqty int(64) NOT NULL DEFAULT 0,
                                                itemName varchar(255) NOT NULL,
                                                price float NOT NULL DEFAULT 0, 
                                                category int(64),
                                                UNIQUE (id),
                                                FOREIGN KEY (category) REFERENCES category (id) ON DELETE SET NULL);

        INSERT INTO category VALUES (88, 'Sample Category');

        INSERT INTO items VALUES (99999, 1, 1, 'Sample Item', 99.99, 88);
     ********************************************************************************************
     * 
     * 
     * 
     */
    public void createTables() {
        try {
        // drop items table
            preparedStatement = conn.prepareStatement(""
                    + "DROP TABLE items");
            preparedStatement.execute();
        // drop category table
            preparedStatement = conn.prepareStatement(""
                    + "DROP TABLE category");
            preparedStatement.execute();
        // create first table "category"
            preparedStatement = conn.prepareStatement(""
                    + "CREATE TABLE category (	id int(64) NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
                        "categoryName varchar(255) NOT NULL, \n" +
                        "UNIQUE (id))");
            preparedStatement.execute();
        // create second table "items"
            preparedStatement = conn.prepareStatement(""
                    + "CREATE TABLE items (id int(64) NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
                        "hsqty int(64) NOT NULL DEFAULT 0,\n" +
                        "elqty int(64) NOT NULL DEFAULT 0,\n" +
                        "itemName varchar(255) NOT NULL,\n" +
                        "price float NOT NULL DEFAULT 0, \n" +
                        "category int(64),\n" +
                        "UNIQUE (id),\n" +
                        "FOREIGN KEY (category) REFERENCES category (id) ON DELETE SET NULL)");
            preparedStatement.execute();
        // insert sample category
            preparedStatement = conn.prepareStatement(""
                    + "INSERT INTO category VALUES (88, 'Sample Category')");
            preparedStatement.execute();
        // insert sample item
            preparedStatement = conn.prepareStatement(""
                    + "INSERT INTO items VALUES (99999, 1, 1, 'Sample Item', 99.99, 88)");
            preparedStatement.execute();
        } catch (SQLException e) {
            System.out.println("Error creating table or tables exist: " + e.getMessage());
        }
    }

    /**
     *
     * @param hsqty
     * @param elqty
     * @param name
     * @param category
     *
     * Inserts an item into the database.
     * @throws java.sql.SQLException
     */
    public void insertItem(String hsqty, String elqty, String name, String price, String category) throws SQLException {
        try {
            //check if category already exists
            preparedStatement = conn.prepareStatement(""
                    + "SELECT id FROM category WHERE categoryName = ?");
            preparedStatement.setString(1, category);
            ResultSet rs = preparedStatement.executeQuery();

            //if the category doesn't exist, add it
            if (!rs.next()) {
                preparedStatement = conn.prepareStatement(""
                        + "INSERT INTO category"
                        + "(categoryName)"
                        + "VALUES "
                        + "(?)");
                preparedStatement.setString(1, category);
                preparedStatement.execute();
            }
            //finally, add the item to the database
            preparedStatement = conn.prepareStatement(""
                    + "INSERT INTO items"
                    + "(hsqty, elqty, itemName, price, category)"
                    + "VALUES "
                    //nested select returns the category id from parent relation
                    + "(?, ?, ?, ?, (SELECT id FROM category WHERE categoryName = ?) )");
            preparedStatement.setString(1, hsqty);
            preparedStatement.setString(2, elqty);
            preparedStatement.setString(3, name);
            preparedStatement.setString(4, price);
            preparedStatement.setString(5, category);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     *
     * @param IDtoDelete
     * @return
     *
     * Deletes an item.  Returns true if item is last in category.
     *
     */
    public boolean deleteItem(int IDtoDelete) {
        boolean found = false;

        try {
            ResultSet rs;

            //get category of item to be deleted
            preparedStatement = conn.prepareStatement(""
                    + "SELECT categoryName "
                    + "FROM items, category "
                    + "WHERE items.category = category.id "
                    + "AND items.id = ?");
            preparedStatement.setString(1, Integer.toString(IDtoDelete));
            rs = preparedStatement.executeQuery();

            rs.next();

            String category = rs.getString("categoryName");

            //delete the item
            preparedStatement = conn.prepareStatement(""
                    + "DELETE FROM items "
                    + "WHERE id = ? "
                    + "LIMIT 1");
            preparedStatement.setString(1, Integer.toString(IDtoDelete));
            preparedStatement.execute();

            //is deleted item's category used by other items?
            rs = getDistinctCategories();

            while (rs.next()) {
                if (rs.getString("categoryName").equals(category)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                //delete the category
                preparedStatement = conn.prepareStatement(""
                        + "DELETE FROM category "
                        + "WHERE categoryName = ? "
                        + "LIMIT 1");
                preparedStatement.setString(1, category);
                preparedStatement.execute();

                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error deleting item." + e.getMessage());
            System.exit(3);
        }
        return false;
    }

    /**
     *
     * @param index
     * @param qty
     * @param location
     *
     * Adds qty to the amount stored in the database.
     */
    public void updateQTY(int index, int qty, int location) {
        try {

            switch (location) {
                case 1:
                    preparedStatement = conn.prepareStatement("SELECT hsqty from items WHERE id = ?");
                    break;
                case 2:
                    preparedStatement = conn.prepareStatement("SELECT elqty from items WHERE id = ?");
                    break;
                default:
                    throw new Exception("Exception in updateQTY.  location is invalid.");
            }

            preparedStatement.setString(1, Integer.toString(index));
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();

            int newQTY = 0;

            switch (location) {
                case 1:
                    preparedStatement = conn.prepareStatement("UPDATE items SET hsqty = ? WHERE id = ?");
                    newQTY = Integer.parseInt(rs.getString("hsqty"));
                    break;
                case 2:
                    preparedStatement = conn.prepareStatement("UPDATE items SET elqty = ? WHERE id = ?");
                    newQTY = Integer.parseInt(rs.getString("elqty"));
                    break;
                default:
                    throw new Exception("Exception in updateQTY.  location is invalid.");
            }

            newQTY += qty;
            preparedStatement.setString(1, Integer.toString(newQTY));
            preparedStatement.setString(2, Integer.toString(index));
            preparedStatement.execute();

        } catch (SQLException e) {
            System.out.println("Error updating quantity: " + e.getMessage());
            System.exit(5);
        } catch (Exception e) {
            System.out.println("Error updating quantity: " + e.getMessage());
        }
    }

    /**
     *
     * @param filterBy
     * @return
     *
     * Returns all items filtered by category and sorted by name.
     *
     * "ALL" value returns all items.
     *
     */
    public ResultSet getAllItemsByCategory(String filterBy) {
        try {
            if (filterBy.equals("ALL")) {
                preparedStatement = conn.prepareStatement(""
                        + "SELECT items.id, items.hsqty, items.elqty, items.itemName, items.price, category.categoryName "
                        + "FROM items, category "
                        + "WHERE items.category = category.id "
                        + "ORDER BY items.itemName",
                        //must be scroll insensitive so I can use again to produce a PDF report
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
            } else {
                preparedStatement = conn.prepareStatement(""
                        + "SELECT items.id, items.hsqty, items.elqty, items.itemName, items.price, category.categoryName "
                        + "FROM items, category "
                        + "WHERE items.category = category.id "
                        + "AND category.categoryName = '" + filterBy + "' "
                        + "ORDER BY items.itemName",
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
            }
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            System.out.println("Exception in getAllItemsByCategory(): " + e.getMessage());
        }
        return null;
    }

    /**
     *
     * @return
     *
     * Returns a distinct categories.
     *
     */
    public ResultSet getDistinctCategories() {
        try {
            preparedStatement = conn.prepareStatement(""
                    + "SELECT DISTINCT categoryName from category, items "
                    + "WHERE category.id = items.category "
                    + "ORDER BY categoryName");
            return preparedStatement.executeQuery();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     *
     * @param ID
     * @param newPrice
     *
     * Sets a new price value.  Does not increment.
     *
     */
    public void updatePrice(int ID, String newPrice) {
        try {
            preparedStatement = conn.prepareStatement("UPDATE items SET price = ? WHERE id = ?");
            preparedStatement.setString(1, newPrice);
            preparedStatement.setString(2, Integer.toString(ID));
            preparedStatement.execute();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     *
     * @param ID
     * @param newName
     *
     * Sets a new name value.
     */
    public void updateName(int ID, String newName) {
        try {
            preparedStatement = conn.prepareStatement("UPDATE items SET itemName = ? WHERE id = ?");
            preparedStatement.setString(1, newName);
            preparedStatement.setString(2, Integer.toString(ID));
            preparedStatement.execute();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}