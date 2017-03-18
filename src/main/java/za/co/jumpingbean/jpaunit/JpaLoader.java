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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.*;

import za.co.jumpingbean.jpaunit.exception.CannotConvertException;
import za.co.jumpingbean.jpaunit.exception.ConnectionException;
import za.co.jumpingbean.jpaunit.exception.JpaLoaderException;
import za.co.jumpingbean.jpaunit.exception.ParserStreamException;
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
    final Map<Class, Map<Object, Object>> dataSetClasses = new LinkedHashMap<>();
    //Counter to log how many enitites where loaded
    private final AtomicInteger count = new AtomicInteger(0);
    EntityManager em;
    JPAParser parser;

    /**
     * Load converter functions supplied by library and custom converter types
     * defined by client. All converters need to implement the
     * @link{Converter} interface
     */
    static {
        ServiceLoader<FieldConverter> foundParsers
                = ServiceLoader.load(FieldConverter.class);

        foundParsers.forEach(c -> {
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

        foundConstructors.forEach(c -> {
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
        //setDefaultErrorHanlder();
        dataset.clear();
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
                                        "Error committing load for test. Closing connection ...", ex);
                        if (em.isOpen()) {
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

    public Optional<Field> getIdAnnotatedField(Class<?> clazz) {
        Collection<Field> fields = new ArrayList<>();
        Class<?> currentClazz = clazz;
        while(currentClazz != Object.class) {
            fields.addAll(Arrays.asList(currentClazz.getDeclaredFields()));
            currentClazz = currentClazz.getSuperclass();
        }

        for (Field field : fields) {
            for (Annotation annotation : field.getAnnotations()) {
                if(annotation.annotationType().equals(Id.class)) {
                    System.out.println("Field return type: " + field.getType());
                    return Optional.of(field);
                }
            }
        }

        return Optional.empty();
    }

    public Optional<Method> getIdAnnotatedMethod(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                if(annotation.annotationType().equals(Id.class)) {
                    return Optional.of(method);
                }
            }
        }

        return Optional.empty();
    }

    private void process() {
        count.set(0);
        dataset.stream().forEach(entry -> {
            Class<?> clazz = entry.getClazz();

            Logger.getLogger(JpaLoader.class
                    .getName()).log(Level.INFO, MessageFormat
                            .format("Processing {0} with id {1}",
                                    clazz, entry.getValue("id")));

            try {
                Object obj = clazz.newInstance();
                //Set object properties
                updateObjectFields(obj, Utility.getAllFields(clazz), entry);
                updateObjectMethods(obj, Utility.getAllMethods(clazz), entry);
                try {
                    //Create linked list in dataSetClasses table to
                    //prevent null pointer checks
                    if (!dataSetClasses.containsKey(clazz)) {
                        dataSetClasses.put(clazz, new HashMap<>());
                    }
                    //try and merge the object. If data exists or someother
                    //error catch exception.
                    Object id = null;

                    Optional<Field> idAnnotatedFieldOptional = getIdAnnotatedField(clazz);
                    Optional<Method> idAnnotatedMethodOptional = getIdAnnotatedMethod(clazz);
                    if(idAnnotatedFieldOptional.isPresent()) {
                        Field idAnnotatedField = idAnnotatedFieldOptional.get();
                        idAnnotatedField.setAccessible(true);
                        Class<?> idType = idAnnotatedField.getType();
                        id = idAnnotatedField.get(obj);

                        if (idType.equals(Long.class) || idType.equals(long.class)) {
                            idAnnotatedField.set(obj, -1L);
                        } else if (idType.equals(Double.class) || idType.equals(double.class)
                                || idType.equals(Float.class) || idType.equals(float.class)) {
                            idAnnotatedField.set(obj, -1.0);
                        } else if (idType.equals(int.class)
                                || idType.equals(short.class) || idType.equals(byte.class)
                            || null != idType.getSuperclass() && idType.getSuperclass().equals(Number.class)) {
                            idAnnotatedField.set(obj, -1);
                        }
                    } else if (idAnnotatedMethodOptional.isPresent()) {
                        Method idAnnotatedMethod = idAnnotatedMethodOptional.get();
                        idAnnotatedMethod.setAccessible(true);
                        id = idAnnotatedMethod.invoke(obj);

                        String methodName = idAnnotatedMethod.getName();
                        if(methodName.startsWith("get")) {
                            String setterMethod = methodName.replaceFirst("get", "set");

                            Method method = obj.getClass().getMethod(setterMethod, idAnnotatedMethod.getReturnType());

                            if (id instanceof Long || id.getClass().equals(long.class)) {
                                method.invoke(obj, -1L);
                            } else if (id instanceof Double || id.getClass().equals(double.class)
                                    || id instanceof Float || id.getClass().equals(float.class)) {
                                method.invoke(obj, -1.0);
                            } else if (id.getClass().equals(int.class)
                                    || id.getClass().equals(short.class) || id.getClass().equals(byte.class)
                                    || null != id.getClass().getSuperclass() && id.getClass().getSuperclass().equals(Number.class)) {
                                method.invoke(obj, -1);
                            }
                        }
                    }
                    obj = this.em.merge(obj);
                    em.flush();
                    //Add dataset object to cache of dataset objects as it
                    //may be used in a foregin key relationship later. Need
                    //original id to look up real entity id.
                    dataSetClasses.get(clazz).put(id, id);
                    Logger.getLogger(JpaLoader.class.getName()).log(Level.INFO,
                            MessageFormat.format("Loaded {0} entity -- {1} ", clazz,
                                    obj.toString()));
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(JpaLoader.class
                            .getName()).log(Level.SEVERE,
                                    MessageFormat.format("Reflection error", clazz), ex);
                } catch (PersistenceException ex) {
                    Logger.getLogger(JpaLoader.class
                            .getName()).log(Level.SEVERE,
                                    MessageFormat.format("Persistence error for class {0}", clazz), ex);
                    throw ex;
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

    private void updateObjectMethods(final Object obj, List<Method> methods, DataSetEntry entry)
            throws IllegalAccessException, InstantiationException {

        //Iterate over set methods on Entity and populate entity with
        //element attribute values if they are defined
        methods.forEach((Method currMethod) -> {

            String methodName = currMethod.getName();
            if (methodName.startsWith("get")) {
                String setterMethodName = methodName.replaceFirst("get", "set");

                Method setterMethod = null;
                try {
                    setterMethod = obj.getClass().getMethod(setterMethodName, currMethod.getReturnType());

                    if (null != setterMethod) {
                        Class parameterClass = currMethod.getReturnType();
                        if (parameterClass.isPrimitive()) {
                            //Determine if this is a primitiive type
                            parameterClass = getPrimitiveType(parameterClass);
                        }
                        //If it is a  simple data type set the properties
                        //remove set from function name to obtain property name
                        //String name = currMethod.getName();
                        if (converters.containsParser(parameterClass)
                                && (entry.getProperties().contains(currMethod.getName()))) {
                            try {
                                Object result = converters.getParser(parameterClass)
                                        .parse(entry.getValue(currMethod.getName()));

                                this.setMethod(setterMethod, obj, result);

                            } catch (CannotConvertException ex) {
                                Logger.getLogger(JpaLoader.class
                                        .getName()).log(Level.SEVERE,
                                        MessageFormat.format("Error converting {0} with value {1}",
                                                currMethod.getName(), entry.getValue(currMethod.getName())), ex);
                            }
                            //Removed used properties from list.
                            //Any remaining properties after load will
                            //be properties that were not matched.
                            entry.removeProperty(currMethod.getName());
                        }
                    }
                } catch (NoSuchMethodException e) {
                    Logger.getLogger(JpaLoader.class
                            .getName()).log(Level.INFO, "{0} has "
                            + "no setter method named {1}",
                            new Object[] {currMethod.getDeclaringClass(), currMethod.getName().replaceFirst("get", "set")});
                }
            }
        });
    }

    private Class getPrimitiveType(Class parameterClass) {
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
        return parameterClass;
    }

    private void setMethod(Method method, Object object, Object value) {
        try {
            boolean wasPrivate = false;
            if(!method.isAccessible()) {
                wasPrivate = true;
                method.setAccessible(true);
            }

            method.invoke(object, value);

            if(wasPrivate) {
                method.setAccessible(false);
            }
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(JpaLoader.class
                    .getName()).log(Level.SEVERE, MessageFormat.format("Reflection error "
                    + "while calling set method {0} to value {1}", method, value), ex);
            throw new ParserStreamException(ex,
                    "Reflection error while processing xml data file");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void updateObjectFields(final Object obj, List<Field> fields, DataSetEntry entry)
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
                    Object candidateObjectId = dataSetClasses.get(parameterClass).get(foreignId);
                    Object candidateObject = em.find(parameterClass, candidateObjectId);
                    this.setField(currField, obj, candidateObject);
                    entry.removeProperty(propertyName.toString());
                    count.incrementAndGet();

                }

            } else if (parameterClass.getDeclaredAnnotation(Embeddable.class) != null) {
                //Determine if this is a complex data type. i.e and
                //@embeddable data type that has its own properties
                //and values
                Object embeddedObj;

                try {
                    embeddedObj = parameterClass.newInstance();
                    updateObjectFields(embeddedObj, Utility.getAllFields(parameterClass), entry);
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
                        if (enumerated != null && enumerated.value() == EnumType.STRING) {
                            enumVal = Enum.valueOf(parameterClass, entry.getValue(property));
                        } else {
                            enumVal = parameterClass.getEnumConstants()[Integer.parseInt(entry.getValue(property))];
                        }
                    } if (enumVal != null) {
                        this.setField(currField, obj, enumVal);
                        entry.removeProperty(property);

                    } else {
                        Logger.getLogger(JpaLoader.class
                                .getName()).log(Level.WARNING, "Enum lookup failed for {0}", parameterClass);
                    }

                } catch (NoSuchFieldException | SecurityException ex) {
                    Logger.getLogger(JpaLoader.class
                            .getName()).log(Level.SEVERE,
                            MessageFormat.format("Error converting enum type {0}",
                                    parameterClass), ex);
                }
            } else if (Collection.class.isAssignableFrom(parameterClass)) {
                //check if this is a many-to-many collection
                try {
                    ParameterizedType type = (ParameterizedType) currField.getGenericType();
                    final Class tmpParameterClass = parameterClass;
                    final Class actualType = (Class) type.getActualTypeArguments()[0];
                    if (entry.getManyToManyRelationships().containsKey(actualType.getName())) {
                        List<Integer> ids = entry.getManyToManyRelationships()
                                .get(actualType.getName());
                        Collection coll;
                        if (List.class.isAssignableFrom(tmpParameterClass)) {
                            coll = new LinkedList();
                        } else {
                            coll = new HashSet();
                        }
                        ids.forEach((Integer id) -> {
                            try {
                                Object tmpObj = actualType.newInstance();
                                Method setId = tmpObj.getClass().getMethod("setId", Integer.class);
                                setId.invoke(tmpObj, id);
                                tmpObj = this.lookupEntity(tmpObj);
                                if (tmpObj!=null) coll.add(tmpObj);
                                Logger.getLogger(JpaLoader.class.getName()).log(Level.INFO,
                                        "Updated {0} many-to-many collection on {1}",
                                        new Object[]{tmpObj.getClass(), tmpParameterClass});
                            } catch (NullPointerException ex) {
                                Logger.getLogger(JpaLoader.class.getName()).log(Level.SEVERE,
                                        "Collection {0} in {1} not found in "
                                                + "previously loaded entities.",
                                        new Object[]{tmpParameterClass, currField});
                            } catch (InstantiationException | IllegalAccessException |
                                    NoSuchMethodException | SecurityException |
                                    IllegalArgumentException | InvocationTargetException ex) {
                                Logger.getLogger(JpaLoader.class.getName()).log(Level.SEVERE,
                                        "Error updating collection. Is the owned class "
                                                + "declared before the owning class?", ex);
                            }
                        });
                        this.setField(currField, obj, coll);
                    }
                } catch (ClassCastException ex) {
                    Logger.getLogger(JpaLoader.class
                            .getName()).log(Level.SEVERE,
                            MessageFormat.format("Error converting collection type {0}",
                                    parameterClass), ex);
                }
            } else {
                if (parameterClass.isPrimitive()) {
                    //Determine if this is a primitiive type
                    parameterClass = getPrimitiveType(parameterClass);
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
        //check if the error handler is regisered. If so remove it
        //as we will end up in a loop as the default error handler
        //
        boolean startingWithOpenTransaction = true;
        //make sure we are safe from any bulk updates.
        em.clear();
        try {
            if (!em.getTransaction().isActive()) {
                startingWithOpenTransaction = false;
            }

            if (startingWithOpenTransaction && em.getTransaction().getRollbackOnly()) {
                em.getTransaction().rollback();
                startingWithOpenTransaction = false;
            }

            try {
                if (!startingWithOpenTransaction) {
                    em.getTransaction().begin();
                }
                Set<Class> set = dataSetClasses.keySet();
                Iterator<Class> itr = set.iterator();
                if (itr.hasNext()) {
                    delete(itr.next(), itr);
                }
                dataSetClasses.clear();
            } finally {
                if (!startingWithOpenTransaction) {
                    if (em.getTransaction().getRollbackOnly()) {
                        em.getTransaction().rollback();
                    } else {
                        em.getTransaction().commit();

                    }
                }
            }
        } catch (PersistenceException | JpaLoaderException ex) {
            Logger.getLogger(JpaLoader.class
                    .getName()).log(Level.SEVERE,
                            "Cleaning database of inserted dataset records failed. "
                            + "Subsequent tests may fail. It is likely your "
                            + "test inserted a record. "
                            + "Please delete manually at end of test", ex);
        }
    }

    private void delete(Class clazz, Iterator<Class> itr) {
        Map<Object, Object> map = dataSetClasses.get(clazz);
        if (itr.hasNext()) {
            delete(itr.next(), itr);
        }
        map.entrySet().forEach(c -> {
            try {
                Object id = c.getValue();
                //determine if entity stille exists & delete if so.
                if (em.find(clazz, id) != null) {
                    Object obj2 = em.find(clazz, id);
                    em.remove(obj2);
                }
            } catch (PersistenceException ex) {
                Logger.getLogger(JpaLoader.class
                        .getName()).log(Level.WARNING, MessageFormat.format(
                                        "Persistence exception deleting {0} object from dataset.",
                                        c), ex);
                throw new JpaLoaderException("Persistence error during delete", ex);
            } catch (Exception ex) {
                Logger.getLogger(JpaLoader.class
                        .getName()).log(Level.WARNING, MessageFormat.format(
                                        "Unexpected error deleting {0} object from dataset.",
                                        c), ex);
                throw new JpaLoaderException("Unexpected error during delete", ex);
            }
        });
    }

    public <E> E lookupEntity(E reference) {
        try {
            Class c = reference.getClass();
            Object id = c.getMethod("getId").invoke(reference);
            if (dataSetClasses.get(c).get(id) != null) {
                return (E) em.find(c, dataSetClasses.get(c).get(id));
            } else {
                return null;
            }
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                InvocationTargetException | SecurityException |NullPointerException ex) {
            Logger.getLogger(JpaLoader.class
                    .getName()).log(Level.WARNING, MessageFormat.format("Look up "
                                    + "for class {0} failed. Entity may not "
                            + "yet be loaded in look up table", 
                            reference.getClass()), ex);
            return null;
        }
    }
}
