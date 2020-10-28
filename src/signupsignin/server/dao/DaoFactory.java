/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package signupsignin.server.dao;

import interfaces.Signable;

/**
 *
 * @author Aketza
 */
public class DaoFactory {

  

    public static Signable getSignable() {
         return new MySQLDaoImplementation();
    }
    }
      
        
      
    

