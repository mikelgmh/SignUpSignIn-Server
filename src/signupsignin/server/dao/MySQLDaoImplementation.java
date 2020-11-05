package signupsignin.server.dao;

import exceptions.EmailAlreadyExistsException;
import exceptions.ErrorClosingDatabaseResources;
import exceptions.ErrorConnectingDatabaseException;
import exceptions.PasswordMissmatchException;
import exceptions.QueryException;
import exceptions.UserAlreadyExistException;
import exceptions.UserAndEmailAlreadyExistException;
import exceptions.UserNotFoundException;
import interfaces.Signable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import user.User;

/**
 * It does the querys with the database.
 *
 * @author Imanol, Mikel
 */
public class MySQLDaoImplementation implements Signable {

    private PreparedStatement ps;
    private ResultSet rs;
    private Connection con;
    private final String insertUser = "INSERT INTO user(login,email,fullname,password,status,privilege) VALUES(?,?,?,?,?,?)";
    private final String checkUser = "SELECT * FROM USER WHERE LOGIN=?";
    private final String checkPassword = "SELECT * FROM USER WHERE LOGIN=? AND PASSWORD=?";
    private final String insertAccess = "UPDATE USER SET LASTACCESS =? WHERE LOGIN=?";
    private final String checkIfUserExists = "SELECT * FROM USER WHERE LOGIN=? OR EMAIL=?";

    /**
     * It does the querys to check if the login and the password are correct
     * with the ones in the database.
     *
     * @param user it contains the login, password data.
     * @return the user with the information that the database have returned.
     * @throws ErrorConnectingDatabaseException if it doesn't connect with the
     * database.
     * @throws UserNotFoundException if an user doesn't exist in the database.
     * @throws PasswordMissmatchException if the password is not the correct one
     * for that user.
     * @throws ErrorClosingDatabaseResources if it fails closing the querys.
     * @throws QueryException when it execute the query it fails.
     */
    @Override
    public User signIn(User user) throws ErrorConnectingDatabaseException, UserNotFoundException, PasswordMissmatchException, ErrorClosingDatabaseResources, QueryException {
        try {
            // Obtengo una conexión desde el pool de conexiones.
            con = ConnectionPool.getConnection();

            //Hago comprobación de que exista el usuario.
            checkUser(user);

            //Establezco el preparedstatement y ejecuto la query. 
            ps = con.prepareStatement(checkPassword);
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getPassword());
            rs = ps.executeQuery();

            //Controlo el error de la contraseña.
            if (!rs.next()) {
                throw new PasswordMissmatchException();
            }
            //Obtengo datos que voy a devolver al clente.
            user.setFullName(rs.getString("FULLNAME"));
            user.setLastAccess(rs.getDate("LASTACCESS"));

            insertAccesTime(user);
            //Control de error de conexión/query incorrecta.
        } catch (SQLException ex1) {
            throw new QueryException();
        } finally {
            try {
                closeConnection();
            } catch (SQLException ex) {
                throw new ErrorClosingDatabaseResources();
            }
        }

        //Devuelvo user
        return user;
    }

    /**
     * It does the check if user doesn't exist and the new user is added to the
     * database.
     *
     * @param user with data to register into the database.
     * @return user with the correct data.
     * @throws UserAlreadyExistException if the user exist in the database.
     * @throws exceptions.UserAndEmailAlreadyExistException
     * @throws exceptions.EmailAlreadyExistsException
     * @throws QueryException if the query fails.
     * @throws ErrorConnectingDatabaseException if the connection to the
     * database fails.
     */
    @Override
    public User signUp(User user) throws UserAlreadyExistException,UserAndEmailAlreadyExistException, EmailAlreadyExistsException, QueryException, ErrorConnectingDatabaseException {
        try {
            this.con = ConnectionPool.getConnection();
            this.checkifUserExists(user);

            this.ps = con.prepareStatement(this.insertUser);
            this.ps.setString(1, user.getLogin());
            this.ps.setString(2, user.getEmail());
            this.ps.setString(3, user.getFullName());
            this.ps.setString(4, user.getPassword());
            this.ps.setString(5, user.getStatus().toString());
            this.ps.setString(6, user.getPrivilege().toString());
            this.ps.execute();
            this.closeConnection();

        } catch (SQLException ex) {

            ex.printStackTrace();
            throw new QueryException();
        } finally {
            try {
                this.closeConnection();
            } catch (SQLException ex) {
                Logger.getLogger(MySQLDaoImplementation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return user;
    }

    /**
     * It checks if the user exists in the database.
     *
     * @param user with data to register or login into the database.
     * @throws SQLException if an error have happened with the query or the
     * database.
     * @throws UserAlreadyExistException if the user already exist in the database.
     * @throws EmailAlreadyExistsException If the email exists in the database.
     * @throws UserAndEmailAlreadyExistException if both exist in the database.
     * database.
     */
    private void checkifUserExists(User user) throws SQLException, UserAlreadyExistException, EmailAlreadyExistsException, UserAndEmailAlreadyExistException {

        this.ps = con.prepareStatement(this.checkIfUserExists);
        this.ps.setString(1, user.getLogin());
        this.ps.setString(2, user.getEmail());
        this.rs = this.ps.executeQuery();

        while (rs.next()) {
            if (rs.getString("login").equalsIgnoreCase(user.getLogin()) && rs.getString("email").equalsIgnoreCase(user.getEmail())) {
                throw new UserAndEmailAlreadyExistException();
            } else if (rs.getString("login").equalsIgnoreCase(user.getLogin())) {
                throw new UserAlreadyExistException(user);
            } else if (rs.getString("email").equalsIgnoreCase(user.getEmail())) {
                throw new EmailAlreadyExistsException();
            }

        }

    }

    /**
     * It checks if the user is in the database.
     *
     * @param user with data to register or login into the database.
     * @throws UserNotFoundException If the user is not found in the database.
     * @throws SQLException If an error have happened with the query or the
     * database.
     */
    private void checkUser(User user) throws UserNotFoundException, SQLException {
        ps = con.prepareStatement(checkUser);
        ps.setString(1, user.getLogin());
        rs = ps.executeQuery();
        if (!rs.next()) {
            throw new UserNotFoundException();
        }
    }

    /**
     * It is to change the last access of the user.
     *
     * @param user with data to register or login into the database.
     * @throws SQLException If an error have happened with the query or the
     * database.
     */
    private void insertAccesTime(User user) throws SQLException {
        java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
        PreparedStatement ps = con.prepareStatement(insertAccess);
        ps.setTimestamp(1, date);
        ps.setString(2, user.getLogin());
        ps.executeUpdate();
        ps.close();
    }

    /**
     * It close the connection.
     *
     * @throws SQLException if an error have happened with the query or the
     * database.
     */
    private void closeConnection() throws SQLException {
        if (this.rs != null) {
            this.rs.close();
        }
        if (this.ps != null) {
            this.ps.close();
        }
        if (this.con != null) {
            this.con.close();
        }
    }
}
