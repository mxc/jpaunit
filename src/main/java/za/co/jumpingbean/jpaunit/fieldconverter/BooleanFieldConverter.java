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

import za.co.jumpingbean.jpaunit.exception.CannotConvertException;

/**
 *
 * @author mark
 */
public class BooleanFieldConverter implements FieldConverter<Boolean> {

    @Override
    public Boolean parse(String elm) throws CannotConvertException {
        switch (elm) {
            case "1":
                elm = "true";
                break;
            case "0":
                elm = "false";
                break;
            case "false":
            case "true":
                break;
            default:
                throw  new CannotConvertException(Boolean.class, elm);
        }
        return  elm.equals("true")? Boolean.TRUE: Boolean.FALSE;
    }

}
