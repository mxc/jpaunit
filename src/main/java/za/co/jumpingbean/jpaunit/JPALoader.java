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
import za.co.jumpingbean.jpaunit.loader.SaxHandler;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Embeddable;
import javax.persistence.EntityManager;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import za.co.jumpingbean.jpaunit.exception.ConverterStreamException;
import za.co.jumpingbean.jpaunit.exception.ParserException;
import za.co.jumpingbean.jpaunit.loader.JPAParser;

/**
 *
 * @author mark
 */
public class JPALoader {

    String modelPackageName;
    String dataSetFileName;
    //Our list of converters found on the classpath
    final Converters converters = new Converters();
    //The data to be provided by a JPAParser
    List<DataSetEntry> dataset = new ArrayList<>();
    //Keep list of classes added during load for foreign key lookup
    final Map<Class, List> dataSetClasses = new LinkedHashMap<>();
    //Counter to log how many enitites where loaded
    private final AtomicInteger count = new AtomicInteger(0);
    EntityManager em;
    JPAParser parser;

    public void init(String dataSetFileName, String modelPackageName, JPAParser parser, EntityManager em) {
        this.modelPackageName = modelPackageName;
        this.dataSetFileName = dataSetFileName;
        this.em = em;
        this.parser = parser;
        this.loadConverters();
    }

    /**
     * Load converter functions supplied by library and custom converter types
     * defined by client. All converters need to implement the @link{Converter}
     * interface
     */
    private void loadConverters() {
        List<ClassLoader> classLoadersList = new LinkedList<>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());
        Set<URL> urls = ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0]));
        urls.addAll(ClasspathHelper.forManifest());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false), new ResourcesScanner())
                .setUrls(urls));

        Set<Class<? extends Converter>> converterImpls = reflections.getSubTypesOf(Converter.class);
        converterImpls.forEach(c -> {
            try {
                Method m = c.getMethod("convert", String.class);
                converters.addConverter(m.getReturnType(), c.newInstance());
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException ex) {
                Logger.getLogger(JPALoader.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

    }

    public void load() throws ParserException {
        this.importDataSet();
        em.getTransaction().begin();
        this.process();
        em.getTransaction().commit();
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
            try {
                Method[] methods = clazz.getMethods();
                Object obj = clazz.newInstance();
                //Set object properties
                updateObject(obj, methods, entry);
                //Add dataset object to cache of dataset objects as it
                //may be used in a foregin key relationship later
                if (!dataSetClasses.containsKey(clazz)) {
                    dataSetClasses.put(clazz, new LinkedList());
                }
                dataSetClasses.get(clazz).add(obj);
                this.em.merge(obj);
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(JPALoader.class.getName()).log(Level.SEVERE,
                        MessageFormat.format("No class found for {0}. Do you have a spelling "
                                + "mistake in your dataset file?", clazz), ex);
            }

            //Print out attributes not loaded into object. These will be unmatched
            //properties due to spelling errors or name mismatch between
            //method name and property name
            Logger.getLogger(JPALoader.class.getName()).log(Level.INFO, "Loaded {0} "
                    + "proeprties for {1}", new Object[]{count.get(), clazz});
            Logger.getLogger(JPALoader.class.getName()).log(Level.INFO, "{0} has "
                    + "the following unmatched attributes", clazz);
            entry.getProperties().stream().forEach(c -> {
                Logger.getLogger(JPALoader.class.getName()).log(Level.INFO, "{0} = {1}",
                        new Object[]{c, entry.getValue(c)});
            });
        });
    }

    private void updateObject(Object obj, Method[] methods, DataSetEntry entry)
            throws IllegalAccessException, InstantiationException {
        //Iterate over set methods on Entity and populate entity with 
        //element attribute values if they are defined
        Arrays.stream(methods).filter(m -> m.getParameterCount() == 1)
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> m.getName().startsWith("set"))
                .forEach(m -> {
                    Class parameterClass = m.getParameterTypes()[0];

                    //Deterimine if this is a foreginObjectReference
                    if (dataSetClasses.containsKey(parameterClass)) {
                        //All foregin keys must end with _id and must return an Integer
                        //We assume the variable name is the same as the method name
                        //sans the "set" part and _id added
                        StringBuilder propertyName = new StringBuilder(m.getName().substring(3));
                        propertyName.append("_id");
                        if (entry.getProperties().contains(propertyName.toString())) {
                            Integer foreignId = Integer.parseInt(entry.getValue(propertyName.toString()));
                            List candidateObjects = dataSetClasses.get(parameterClass);
                            //Find previously created dataset object and set it on current object
                            candidateObjects.forEach((Object dataSetObject) -> {
                                try {
                                    Method mgetId = dataSetObject.getClass().getMethod("getId");
                                    Integer dataSetObjId = (Integer) mgetId.invoke(dataSetObject);
                                    if (Objects.equals(dataSetObjId, foreignId)) {
                                        //set foreign object
                                        m.invoke(obj, dataSetObject);
                                        count.incrementAndGet();
                                    }
                                } catch (NoSuchMethodException | SecurityException |
                                IllegalAccessException | IllegalArgumentException |
                                InvocationTargetException ex) {
                                    Logger.getLogger(JPALoader.class.getName()).log(Level.SEVERE,
                                            "Error executing 'getId' method on class"
                                            + dataSetObject.getClass(), ex);
                                }
                            });
                        }
                    } else {
                        //Determine if this is a complex data type. i.e and
                        //@embeddable data type that has its own properties
                        //and values
                        Annotation annotation = parameterClass.getDeclaredAnnotation(Embeddable.class);
                        if (annotation != null) {
                            Object embeddedObj;
                            try {
                                embeddedObj = parameterClass.newInstance();
                                updateObject(embeddedObj, parameterClass.getDeclaredMethods(), entry);
                                m.invoke(obj, embeddedObj);
                            } catch (InstantiationException | IllegalAccessException |
                            IllegalArgumentException | InvocationTargetException ex) {
                                Logger.getLogger(JPALoader.class.getName()).log(Level.SEVERE,
                                        "Reflection error while processing xml data file", ex);
                                throw new ConverterStreamException(ex, "Reflection error while "
                                        + "processing xml data file");
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
                            String property = m.getName().substring(3);
                            //int index = entry.getIndexOfPropert(property);
                            if (!property.isEmpty()) {
                                if (converters.contains(parameterClass)) {
                                    Object result = converters.get(parameterClass)
                                    .convert(entry.getValue(property));
                                    try {
                                        m.invoke(obj, result);
                                        count.incrementAndGet();
                                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                        Logger.getLogger(JPALoader.class.getName()).log(Level.SEVERE, "Reflection error while processing xml data file", ex);
                                        throw new ConverterStreamException(ex, "Reflection error while processing xml data file");
                                    }
                                }
                                //Removed used properties from list.
                                //Any remaining properties after load will
                                //be properties that were not matched.
                                entry.removeProperty(property);
                            } else {
                                Logger.getLogger(JPALoader.class.getName()).log(Level.WARNING, "No converter found for {0}", parameterClass);
                            }
                        }
                    }

                });
    }

    public void delete() {
        em.getTransaction().begin();
        Set<Class> set = dataSetClasses.keySet();
        Iterator<Class> itr = set.iterator();
        delete(itr.next(), itr);
        em.getTransaction().commit();
    }

    private void delete(Class clazz, Iterator<Class> itr) {
        List list = dataSetClasses.get(clazz);
        if (itr.hasNext()) {
            delete(itr.next(), itr);
        }
        list.forEach(c -> {
            c = em.merge(c);
            em.remove(c);
        });
    }
}

