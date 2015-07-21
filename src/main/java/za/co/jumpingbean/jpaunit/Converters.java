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
package za.co.jumpingbean.jpaunit;

import za.co.jumpingbean.jpaunit.fieldconverter.FieldConverter;
import java.util.HashMap;
import za.co.jumpingbean.jpaunit.exception.ConverterAlreadyDefinedException;
import za.co.jumpingbean.jpaunit.exception.CannotConvertException;
import za.co.jumpingbean.jpaunit.exception.NoConverterDefinedException;
import java.util.Map;
import za.co.jumpingbean.jpaunit.objectconstructor.ObjectConstructor;

/**
 *
 * @author mark
 */
public class Converters {

    private final Map<Class, FieldConverter> parsers = new HashMap();
    private final Map<Class, ObjectConstructor> constructors = new HashMap();

    public Object convert(Class clazz, String representation) throws NoConverterDefinedException, CannotConvertException {
        if (parsers.containsKey(clazz)) {
            return parsers.get(clazz).parse(representation);
        } else {
            throw new NoConverterDefinedException(clazz, representation);
        }
    }

    public void addParser(Class clazz, FieldConverter parser) throws ConverterAlreadyDefinedException {
        if (parsers.containsKey(clazz)) {
            throw new ConverterAlreadyDefinedException(clazz);
        }
        parsers.put(clazz, parser);
    }

    public boolean containsParser(Class clazz) {
        return parsers.containsKey(clazz);
    }

    public FieldConverter getParser(Class clazz) {
        return parsers.get(clazz);
    }

    public void addObjectConstructor(Class clazz, ObjectConstructor constr) {
        if (parsers.containsKey(clazz)) {
            throw new ConverterAlreadyDefinedException(clazz);
        }
        constructors.put(clazz, constr);
    }

    public boolean containsConstructor(Class clazz) {
        return constructors.containsKey(clazz);
    }

    public ObjectConstructor getConstructor(Class clazz) {
        return constructors.get(clazz);
    }

}
