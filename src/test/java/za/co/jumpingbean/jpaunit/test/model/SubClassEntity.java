/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.jumpingbean.jpaunit.test.model;

import javax.persistence.Entity;

/**
 *
 * @author mark
 */
@Entity
public class SubClassEntity extends AbstractSuperEntity {

    private String stringValue;

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }
    
    
}
