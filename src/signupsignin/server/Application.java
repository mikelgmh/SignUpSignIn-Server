package signupsignin.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application class where it is created the server socket 
 * and then it stay listening indefinitely until it recieve an exit call.
 *
 * @author Mikel
 */
public class Application {

    private static final Logger logger = Logger.getLogger("signupsignin.server.Application");

    //static ServerSocket variable
    private static ServerSocket serverSocket;
    private static final ResourceBundle rb = ResourceBundle.getBundle("config.config");
    private static final Integer maxConnections = Integer.parseInt(ResourceBundle.getBundle("config.config").getString("MAX_CONNECTIONS"));
    private static Integer currentConnections = 0;

    //socket server port on which it will listen
  
    public static void main(String args[]) throws IOException {
        try {
           
            logger.log(Level.INFO, "Starting Server.");
            //create the socket server object
            serverSocket = new ServerSocket(Integer.parseInt(rb.getString("SERVER_SOCKET_PORT")));
            logger.log(Level.INFO, "Server started.");
            //keep listens indefinitely until receives 'exit' call or program terminates
            while (true) {
                //creating socket and waiting for client connection
                if (currentConnections < maxConnections) {
                    Socket socket = serverSocket.accept();
                    new Worker(socket).start();
                    logger.log(Level.INFO, "New thread created.");
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error trying to start the server.");
        }

    }
    /**
     * It is a meter that plus a connection when a thread starts. 
     */
    public synchronized static void sumConnection() {
        currentConnections++;
    }
    /**
     * It is a meter that substract a connection when a thread ends.
     */
    public synchronized static void substractConnection() {
        currentConnections--;
    }
}
