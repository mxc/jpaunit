/* 
 * Copyright (C) 2015 mark
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package za.co.jumpingbean.jpaunit;

import za.co.jumpingbean.jpaunit.converter.Converter;
import java.util.HashMap;
import za.co.jumpingbean.jpaunit.exception.ConverterAlreadyDefinedException;
import za.co.jumpingbean.jpaunit.exception.CannotConvertException;
import za.co.jumpingbean.jpaunit.exception.NoConverterDefinedException;
import java.util.Map;

/**
 *
 * @author mark
 */
public class Converters {
    
    private final Map<Class,Converter> converters = new HashMap();
    
    public Object convert(Class clazz,String representation) throws NoConverterDefinedException, CannotConvertException{
        if (converters.containsKey(clazz)){
            return converters.get(clazz).convert(representation);
        }else{
            throw new NoConverterDefinedException(clazz,representation);
        }
    }
    
    public void addConverter(Class clazz,Converter converter) throws ConverterAlreadyDefinedException{
        if (converters.containsKey(clazz)){
            throw new ConverterAlreadyDefinedException(clazz);
        }
        converters.put(clazz,converter);
    }

    public boolean contains(Class clazz) {
            return converters.containsKey(clazz);
    }
    
    public Converter get(Class clazz){
        return converters.get(clazz);
    }
}
