/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.jumpingbean.jpaunit.objectconstructor;

import za.co.jumpingbean.jpaunit.DataSetEntry;

/**
 *
 * @author mark
 */
@FunctionalInterface
public interface ObjectConstructor {
    public <E> E construct (DataSetEntry entry);
}
