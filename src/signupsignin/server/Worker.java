package signupsignin.server;

import exceptions.EmailAlreadyExistsException;
import exceptions.ErrorClosingDatabaseResources;
import exceptions.ErrorConnectingDatabaseException;
import exceptions.ErrorConnectingServerException;
import exceptions.PasswordMissmatchException;
import exceptions.QueryException;
import exceptions.UserAlreadyExistException;
import exceptions.UserAndEmailAlreadyExistException;
import exceptions.UserNotFoundException;
import interfaces.Signable;
import java.util.logging.Level;
import java.util.logging.Logger;
import message.TypeMessage;
import user.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import message.Message;
import java.net.Socket;
import java.util.ResourceBundle;
import signupsignin.server.dao.DaoFactory;

/**
 * This class is where the thread are created, and it does the message journey.
 *
 * @author Mikel, Imanol
 */
public class Worker extends Thread {

    private static final Logger logger = Logger.getLogger("signupsignin.server.Worker");

    private Socket socket;
    private Message message = null;
    private ObjectInputStream ois;
    private ResourceBundle rb = ResourceBundle.getBundle("config.config");

    public Worker(Socket socket) {
        this.socket = socket;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    /**
     * This method what it does is sending the message to the database and it
     * return the message with the message type changed that is going to be sent
     * to the client.
     */
    @Override
    public void run() {
        Application.sumConnection();
        try {
            logger.log(Level.INFO, "Sending the message to the database.");
            //read from socket to ObjectInputStream object
            ois = new ObjectInputStream(this.socket.getInputStream());
            //convert ObjectInputStream object to Message
            this.message = (Message) ois.readObject();
            Signable dao = DaoFactory.getSignable(rb.getString("DATABASE_TYPE"));
            switch (this.message.getType()) {
                /**
                 * In this switch case we try to sign up a new user and if it is
                 * all correct we send an ok message, if something is wrong
                 * there are different exceptions to catch the error.
                 */
                case SIGN_UP:
                    try {
                    User user = dao.signUp(this.message.getUser());
                    message = new Message(user, TypeMessage.REGISTER_OK);
                } catch (UserAlreadyExistException ex) {
                    logger.log(Level.SEVERE, "The user already exist exception ocurred.");
                    message = new Message(this.message.getUser(), TypeMessage.USER_EXISTS);
                } catch (ErrorConnectingDatabaseException ex) {
                    logger.log(Level.SEVERE, "Error connecting to the database exception ocurred.");
                    message = new Message(this.message.getUser(), TypeMessage.DATABASE_ERROR);
                } catch (QueryException ex) {
                    logger.log(Level.SEVERE, "A query error exception ocurred.");
                    message = new Message(this.message.getUser(), TypeMessage.QUERY_ERROR);
                } catch (ErrorConnectingServerException ex) {
                    Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
                } catch (EmailAlreadyExistsException ex) {
                    Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
                    message = new Message(this.message.getUser(), TypeMessage.EMAIL_EXISTS);
                } catch (UserAndEmailAlreadyExistException ex) {
                    Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
                    message = new Message(this.message.getUser(), TypeMessage.USER_AND_EMAIL_EXIST);
                }
                break;

                /**
                 * In the other case we try to sign in, if it is all correct we
                 * send an ok message, if something goes wrong it catch the
                 * error with the different exceptions that there are.
                 */
                case SIGN_IN:
                    try {
                    User user = dao.signIn(this.message.getUser());
                    message = new Message(user, TypeMessage.REGISTER_OK);
                } catch (ErrorConnectingDatabaseException ex) {
                    logger.log(Level.SEVERE, "Error connecting to the database exception ocurred.");
                    message = new Message(this.message.getUser(), TypeMessage.DATABASE_ERROR);
                } catch (QueryException ex) {
                    logger.log(Level.SEVERE, "A query error exception ocurred.");
                    message = new Message(this.message.getUser(), TypeMessage.QUERY_ERROR);
                } catch (UserNotFoundException ex) {
                    logger.log(Level.SEVERE, "User not found exception ocurred.");
                    message = new Message(this.message.getUser(), TypeMessage.USER_DOES_NOT_EXIST);
                } catch (PasswordMissmatchException ex) {
                    logger.log(Level.SEVERE, "The password does not match exception ocurred.");
                    message = new Message(this.message.getUser(), TypeMessage.LOGIN_ERROR);
                } catch (ErrorClosingDatabaseResources ex) {
                    logger.log(Level.SEVERE, "Error closing the database resources exception ocurred.");
                    message = new Message(this.message.getUser(), TypeMessage.STOP_SERVER);
                } catch (ErrorConnectingServerException ex) {
                    Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;

            }

        } catch (IOException | ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "Error trying to read the message.");
        } /**
         * In this finally clause the server return the message and then it
         * close the socket and the thread ends.
         */
        finally {
            try {
                logger.log(Level.INFO, "Sending the message back to the client.");
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(this.message);
                oos.close();
                ois.close();
                this.socket.close();
                Application.substractConnection();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error trying to send back the message to the client.");
            }
        }

    }
}
