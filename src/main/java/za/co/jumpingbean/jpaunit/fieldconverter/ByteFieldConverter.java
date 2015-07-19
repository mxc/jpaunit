/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.jumpingbean.jpaunit.fieldconverter;

import za.co.jumpingbean.jpaunit.exception.CannotConvertException;

/**
 *
 * @author mark
 */
public class ByteFieldConverter  implements FieldConverter<Byte> {

    @Override
    public Byte parse(String currentElm) throws CannotConvertException {
        try{
            return Byte.parseByte(currentElm);
        }catch(NumberFormatException ex){
            throw new CannotConvertException(Byte.class,currentElm);
        }
    }
    
}
