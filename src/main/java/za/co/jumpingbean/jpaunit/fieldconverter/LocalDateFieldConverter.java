/* 
 * Copyright (C) 2015 Mark Clarke.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package za.co.jumpingbean.jpaunit.fieldconverter;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import za.co.jumpingbean.jpaunit.exception.CannotConvertException;

/**
 *
 * @author mark
 */
public class LocalDateFieldConverter implements FieldConverter<LocalDate> {

    @Override
    public LocalDate parse(String elm) throws CannotConvertException {
           try{
               return LocalDate.parse(elm);
           }catch(DateTimeParseException ex){
               throw new CannotConvertException(LocalDate.class, elm);
           }
    }
    
}
