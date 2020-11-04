package signupsignin.server.dao;

import interfaces.Signable;

/**
 *
 * @author Mikel
 */
public class DaoFactory {

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