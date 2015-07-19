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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Embeddable;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PersistenceException;
import za.co.jumpingbean.jpaunit.exception.CannotConvertException;
import za.co.jumpingbean.jpaunit.exception.ConnectionException;
import za.co.jumpingbean.jpaunit.exception.ParserStreamException;
import za.co.jumpingbean.jpaunit.exception.LookupException;
import za.co.jumpingbean.jpaunit.exception.ParserException;
import za.co.jumpingbean.jpaunit.loader.JPAParser;
import za.co.jumpingbean.jpaunit.objectconstructor.ObjectConstructor;

/**
 *
 * @author mark
 */
public class JpaLoader {

    String modelPackageName;
    String dataSetFileName;
    //Our list of converters found on the classpath
    static final Converters converters = new Converters();
    //The data to be provided by a JPAParser
    List<DataSetEntry> dataset = new ArrayList<>();
    //Keep list of classes added during load for foreign key lookup
    final Map<Class, Map<Integer, Object>> dataSetClasses = new LinkedHashMap<>();
    //Counter to log how many enitites where loaded
    private final AtomicInteger count = new AtomicInteger(0);
    EntityManager em;
    JPAParser parser;

    /**
     * Load converter functions supplied by library and custom converter types
     * defined by client. All converters need to implement the @link{Converter}
     * interface
     */
    static {
        ServiceLoader<FieldConverter> foundParsers
                = ServiceLoader.load(FieldConverter.class);

        foundParsers.forEach(c
                -> {
                    try {
                        Method m = c.getClass().getMethod("parse", String.class);
                        converters.addParser(m.getReturnType(), c);
                        Logger.getLogger((JpaLoader.class.getName())).log(Level.INFO, MessageFormat.format("Method parse found in class {0} ", c));
                    } catch (NoSuchMethodException | SecurityException ex) {
                        Logger.getLogger(JpaLoader.class.getName()).log(Level.SEVERE, MessageFormat.format("Method parse not found in class {0} ", c), ex);
                    }
                }
        );

        ServiceLoader<ObjectConstructor> foundConstructors
                = ServiceLoader.load(ObjectConstructor.class);

        foundConstructors.forEach(c
                -> {
                    try {
                        Method m = c.getClass().getMethod("construct", String.class);
                        converters.addObjectConstructor(m.getReturnType(), c);
                        Logger.getLogger((JpaLoader.class.getName())).log(Level.INFO, MessageFormat.format("Method construct found in class {0} ", c));
                    } catch (NoSuchMethodException | SecurityException ex) {
                        Logger.getLogger(JpaLoader.class.getName()).log(Level.SEVERE, MessageFormat.format("Method construct not found in class {0} ", c), ex);
                    }
                }
        );

    }

    public void init(String dataSetFileName, String modelPackageName, JPAParser parser, EntityManager em) {
        this.modelPackageName = modelPackageName;
        this.dataSetFileName = dataSetFileName;
        this.em = em;
        this.parser = parser;
        Thread.currentThread().setUncaughtExceptionHandler((Thread thread, Throwable thrwbl) -> {
            Logger.getLogger(JpaLoader.class.getName()).log(Level.SEVERE, "Exception thrown in test!. Cleaning database");
            if (em.isOpen()) {
                this.delete();
            }
        });
    }

    /**
     * Utility method to change the loader's entity manager.
     *
     * @param em
     * @throws ConnectionException
     */
    public void setEntityManager(EntityManager em) throws ConnectionException {
        if (em != null && em.isOpen()) {
            throw new ConnectionException("Current entity manager is still open.", null);
        } else {
            this.em = em;
        }
    }

    public void load() throws ParserException {
        this.importDataSet();
        boolean active = em.getTransaction().isActive();
        try {
            if (!active) {
                em.getTransaction().begin();
            }
        } finally {
            this.process();
            if (!active) {
                if (em.getTransaction().getRollbackOnly()) {
                    em.getTransaction().rollback();
                } else {
                    try {
                        em.getTransaction().commit();

                    } catch (PersistenceException ex) {
                        Logger.getLogger(JpaLoader.class
                                .getName()).log(Level.SEVERE,
                                        "**** Error committing load for test. Closing connection ...", ex);
                        if(em.isOpen()){
                            em.close();
                        }
                        throw ex;
                    }
                }
                em.clear();
            }
        }
    }

    /**
     * Load the dataset parser into lists
     *
     * @throws za.co.jumpingbean.jpaunit.exception.ParserException
     */
    private void importDataSet() throws ParserException {
        parser.process(dataSetFileName, modelPackageName);
        this.dataset = parser.getDataEnties();
    }

    private void process() {
        count.set(0);
        dataset.stream().forEach(entry -> {
            Class clazz = entry.getClazz();

            Logger
                    .getLogger(JpaLoader.class
                            .getName()).log(Level.INFO, MessageFormat.format("*** Processing {0} with id {1} ***",
                                    clazz, entry.getValue("id")));

            try {
                List<Field> fields = Utility.getAllFields(clazz);
                Object obj = clazz.newInstance();
                //Set object properties
                updateObject(obj, fields, entry);
                try {
                    //Create linked list in datSetClasses table to
                    //prevent null pointer checks
                    if (!dataSetClasses.containsKey(clazz)) {
                        dataSetClasses.put(clazz, new HashMap<>());
                    }
                    //try and merge the object. If data exists or someother
                    //error catch exception. 
                    Integer id = (Integer) obj.getClass().getMethod("getId").invoke(obj);
                    obj = this.em.merge(obj);
                    //Add dataset object to cache of dataset objects as it
                    //may be used in a foregin key relationship later. Need
                    //original id to look up keep from source file.
                    dataSetClasses.get(clazz).put(id, obj);
                    Logger.getLogger(JpaLoader.class.getName()).log(Level.INFO,
                            MessageFormat.format("Loaded {0} entity ", clazz));
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(JpaLoader.class
                            .getName()).log(Level.SEVERE,
                                    MessageFormat.format("Reflection error", clazz), ex);
                } catch (PersistenceException ex) {
                    Logger.getLogger(JpaLoader.class
                            .getName()).log(Level.SEVERE,
                                    MessageFormat.format("Persistence error for class {0}", clazz), ex);
                }
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(JpaLoader.class
                        .getName()).log(Level.SEVERE,
                                MessageFormat.format("No class found for {0}. Do you have a spelling "
                                        + "mistake in your dataset file?", clazz), ex);
            }

            //Print out attributes not loaded into object. These will be unmatched
            //properties due to spelling errors or name mismatch between
            //method name and property name
            Logger.getLogger(JpaLoader.class
                    .getName()).log(Level.INFO, "Loaded {0} "
                            + "properties for {1}", new Object[]{count.get(), clazz});

            if (!entry.getProperties()
                    .isEmpty()) {
                Logger.getLogger(JpaLoader.class
                        .getName()).log(Level.INFO, "{0} has "
                                + "the following unmatched attributes", clazz);
                entry.getProperties()
                        .stream().forEach(c -> {
                            Logger.getLogger(JpaLoader.class.getName()).log(Level.INFO, "{0} = {1}",
                                    new Object[]{c, entry.getValue(c)});
                        });
            }
        }
        );
    }

    private void updateObject(Object obj, List<Field> fields, DataSetEntry entry)
            throws IllegalAccessException, InstantiationException {

        //Iterate over set methods on Entity and populate entity with 
        //element attribute values if they are defined
        fields.stream().forEach((Field currField) -> {
            Class parameterClass = currField.getType();

            //Deterimine if this is a foreginObjectReference
            if (dataSetClasses.containsKey(parameterClass)) {
                //All foregin keys must end with _id and must return an Integer
                //We assume the variable name is the same as the method name
                //sans the "set" part and _id added
                StringBuilder propertyName = new StringBuilder(currField.getName());
                propertyName.append("_id");
                if (entry.getProperties().contains(propertyName.toString())) {
                    Integer foreignId = Integer.parseInt(entry.getValue(propertyName.toString()));
                    //Find previously created dataset object and set it on current object
                    Object candidateObject = dataSetClasses.get(parameterClass).get(foreignId);
                    this.setField(currField, obj, candidateObject);
                    entry.removeProperty(propertyName.toString());
                    count.incrementAndGet();

                }

            } else if (parameterClass.getDeclaredAnnotation(Embeddable.class
            ) != null) {
                //Determine if this is a complex data type. i.e and
                //@embeddable data type that has its own properties
                //and values
                Object embeddedObj;

                try {
                    embeddedObj = parameterClass.newInstance();
                    updateObject(embeddedObj, Utility.getAllFields(parameterClass), entry);
                    this.setField(currField, obj, embeddedObj);
                } catch (InstantiationException | IllegalAccessException ex) {
                    Logger.getLogger(JpaLoader.class.getName()).log(Level.SEVERE,
                            "Reflection error while processing xml data file", ex);
                    throw new ParserStreamException(ex, "Reflection error while "
                            + "processing xml data file");
                }
            } else if (parameterClass.isEnum()) {
                //If this is an enum the set enum
                try {
                    String property = currField.getName();
                    Object enumVal = null;
                    if (entry.getValue(property) != null) {
                        Field field = obj.getClass().getDeclaredField(property);
                        Enumerated enumerated = field.getAnnotation(Enumerated.class
                        );
                        if (enumerated
                                != null && enumerated.value()
                                == EnumType.STRING) {
                            enumVal = Enum.valueOf(parameterClass, entry.getValue(property));
                        } else {
                            enumVal = parameterClass.getEnumConstants()[Integer.parseInt(entry.getValue(property))];
                        }
                    }
                    if (enumVal != null) {
                        this.setField(currField, obj, enumVal);
                        entry.removeProperty(property);

                    } else {
                        Logger.getLogger(JpaLoader.class
                                .getName()).log(Level.WARNING, "Enum lookup failed for {0}", parameterClass);
                    }

                } catch (NoSuchFieldException | SecurityException ex) {
                    Logger.getLogger(JpaLoader.class
                            .getName()).log(Level.SEVERE,
                                    MessageFormat.format("Error converting enum type {0}", parameterClass), ex);
                }
            } else {
                if (parameterClass.isPrimitive()) {
                    //Determine if this is a primitiive type
                    String primitiveType = parameterClass.getTypeName();

                    switch (primitiveType) {
                        case "int":
                            parameterClass = Integer.class;
                            break;
                        case "char":
                            parameterClass = Character.class;
                            break;
                        case "long":
                            parameterClass = Long.class;
                            break;
                        case "float":
                            parameterClass = Float.class;
                            break;
                        case "double":
                            parameterClass = Double.class;
                            break;
                        case "boolean":
                            parameterClass = Boolean.class;

                            break;
                    }
                }
                //If it is a  simple data type set the properties
                //remove set from function name to obtain property name
                //String name = currField.getName();
                if (converters.containsParser(parameterClass) && entry.getProperties().contains(currField.getName())) {
                    try {
                        Object result = converters.getParser(parameterClass)
                                .parse(entry.getValue(currField.getName()));
                        this.setField(currField, obj, result);

                    } catch (CannotConvertException ex) {
                        Logger.getLogger(JpaLoader.class
                                .getName()).log(Level.SEVERE,
                                        MessageFormat.format("Error converting {0} with value {1}",
                                                currField.getName(), entry.getValue(currField.getName())), ex);
                    }
                    //Removed used properties from list.
                    //Any remaining properties after load will
                    //be properties that were not matched.
                    entry.removeProperty(currField.getName());
                }

            }

        });
    }


    private void setField(Field field, Object object, Object value) {
        try {
            field.setAccessible(true);
            field.set(object, value);
            field.setAccessible(false);

        } catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(JpaLoader.class
                    .getName()).log(Level.SEVERE, MessageFormat.format("Reflection error "
                                    + "while setting field {0} to value {1}", field, value), ex);
            throw new ParserStreamException(ex,
                    "Reflection error while processing xml data file");
        }
    }

    public void delete() {
        try {
            boolean active = true;

            if (!em.getTransaction().isActive()) {
                active = false;
            }
            try {
                em.clear();
                if (!active) {
                    em.getTransaction().begin();
                }
                Set<Class> set = dataSetClasses.keySet();
                Iterator<Class> itr = set.iterator();
                if (itr.hasNext()) {
                    delete(itr.next(), itr);
                }
                dataSetClasses.clear();
            } finally {
                if (!active) {
                    if (em.getTransaction().getRollbackOnly()) {
                        em.getTransaction().rollback();
                    } else {
                        em.getTransaction().commit();

                    }
                }
            }
        } catch (PersistenceException ex) {
            Logger.getLogger(JpaLoader.class
                    .getName()).log(Level.SEVERE,
                            "Cleaning database of inserted dataset records failed. "
                            + "Subsequent tests may fail. It is likely your "
                            + "test inserted a record. "
                            + "Please delete manually at end of test", ex);
        }
    }

    private void delete(Class clazz, Iterator<Class> itr) {
        Map<Integer, Object> map = dataSetClasses.get(clazz);
        if (itr.hasNext()) {
            delete(itr.next(), itr);
        }
        map.entrySet().forEach(c -> {
            try {
                Integer id = (Integer) c.getValue().getClass().getMethod("getId").invoke(c.getValue());
                if (em.find(clazz, id) != null) {
                    Object obj2 = em.find(clazz, id);
                    em.remove(obj2);

                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(JpaLoader.class
                        .getName()).log(Level.WARNING, "Error deleting object from dataset", ex);
            }
        });
    }

    public <E> E lookupEntity(E reference) throws LookupException {
        try {
            Class c = reference.getClass();
            Integer id = (Integer) c.getMethod("getId").invoke(reference);
            //merge and refresh the object
            E obj = em.merge((E)dataSetClasses.get(c).get(id));
            em.refresh(obj);
            return obj;

        } catch (NoSuchMethodException ex) {
            Logger.getLogger(JpaLoader.class
                    .getName()).log(Level.SEVERE, MessageFormat.format("No method "
                                    + "getId for class {0}", reference.getClass()), ex);
            return reference;
        } catch (IllegalAccessException | IllegalArgumentException |
                InvocationTargetException | SecurityException ex) {
            Logger.getLogger(JpaLoader.class
                    .getName()).log(Level.SEVERE, "Error looking up "
                            + "reference object in dataset", ex);
            throw new LookupException(
                    "Error looking up reference entity in dataset", ex);
        }
    }



}
