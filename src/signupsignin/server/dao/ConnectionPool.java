package signupsignin.server.dao;

import exceptions.ErrorConnectingDatabaseException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 * This class takes the connection info from the config file and with it, 
 * it opens the connection with the database.
 * @author Imanol
 */
public class ConnectionPool {

    private static final Logger logger = Logger.getLogger("signupsignin.server.dao.ConnectionPool");

    private static BasicDataSource ds = null;
    private static final ResourceBundle rb = ResourceBundle.getBundle("config.config");
    /**
     * It set the parameters for the connection.
     * @return information from the connection.
     */
    public static DataSource getDataSource() {
        if (ds == null) {
            ds = new BasicDataSource();
            ds.setDriverClassName(rb.getString("driver"));
            ds.setUsername(rb.getString("user"));
            ds.setPassword(rb.getString("password"));
            ds.setUrl(rb.getString("host"));
            //Establecer parametros adecuados	
            ds.setMaxWaitMillis(3000);
        }
        return ds;
    }
       /**
        * 
        * @return an active connection.
        * @throws ErrorConnectingDatabaseException if it can't connect to the database. 
        */
    public static Connection getConnection() throws ErrorConnectingDatabaseException {
        try {
            return getDataSource().getConnection();
        } catch (SQLException ex) {
            throw new ErrorConnectingDatabaseException();
        }
    }
}
