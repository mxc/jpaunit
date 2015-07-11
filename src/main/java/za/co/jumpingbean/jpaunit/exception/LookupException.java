/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.jumpingbean.jpaunit.exception;

/**
 *
 * @author mark
 */
public class LookupException extends RuntimeException {

    public LookupException(String mesg,Throwable ex) {
        super(ex);
        this.addSuppressed(ex);
    }
    
}
