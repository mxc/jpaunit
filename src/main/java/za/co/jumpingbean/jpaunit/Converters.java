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
