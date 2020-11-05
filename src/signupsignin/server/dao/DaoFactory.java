package signupsignin.server.dao;

import interfaces.Signable;

/**
 *
 * @author Mikel
 */
public class DaoFactory {
    /**
     * Gets a Dao implementation.
     * @param type it specify the database connection type.
     * @return an implementation of type dao signable.
     */
    public static Signable getSignable(String type) {
        Signable signable = null;
        switch (type) {
            case "mysql":
                signable = (Signable) new MySQLDaoImplementation();
                break;
        }
        return signable;
    }
}
