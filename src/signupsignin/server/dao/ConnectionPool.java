package signupsignin.server.dao;

import exceptions.ErrorConnectingDatabaseException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 *
 * @author Imanol
 */
public class ConnectionPool {

    private static final Logger logger = Logger.getLogger("signupsignin.server.dao.ConnectionPool");

    private static BasicDataSource ds = null;
    private static final ResourceBundle rb = ResourceBundle.getBundle("config.config");

    public static DataSource getDataSource() {
        if (ds == null) {
            ds = new BasicDataSource();
            ds.setDriverClassName(rb.getString("driver"));
            ds.setUsername(rb.getString("user"));
            ds.setPassword(rb.getString("password"));
            ds.setUrl(rb.getString("host"));
            //Establecer parametros adecuados	
            ds.setMaxTotal(10);
            ds.setMaxWaitMillis(3000);
        }
        return ds;
    }

    public static Connection getConnection() throws ErrorConnectingDatabaseException {
        try {
            return getDataSource().getConnection();
        } catch (SQLException ex) {
            throw new ErrorConnectingDatabaseException();
        }
    }
}
