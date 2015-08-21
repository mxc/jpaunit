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
package za.co.jumpingbean.jpaunit.loader;

import za.co.jumpingbean.jpaunit.exception.ParserException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import za.co.jumpingbean.jpaunit.DataSetEntry;

/**
 *
 * @author mark
 */
/**
 * Nested inner class that implements the SAX parser to load dataset
 */
public class SaxHandler extends DefaultHandler implements JPAParser {

    private final List<DataSetEntry> dataSetEntries = new ArrayList<>();
    private String modelPackageName;
    private int count = 0;
    private int attrCounter = 0;
    private String fileName;

    public SaxHandler() {
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        Logger.getLogger(SaxHandler.class.getName()).log(Level.INFO, "set {0} "
                + "attribute values on {1}", new Object[]{attrCounter, qName});
        Logger.getLogger(SaxHandler.class.getName()).log(Level.INFO, "processed {0}", qName);
        attrCounter = 0;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        //ignore the root element
        if (qName.equalsIgnoreCase("dataset")) {
            return;
        }
        Logger.getLogger(SaxHandler.class.getName()).log(Level.INFO, "processing {0}", qName);
        //Construct package name
        StringBuilder strBuf = new StringBuilder(modelPackageName);
        strBuf.append(".").append(qName);
        String className = strBuf.toString();
        try {
            count++;
            DataSetEntry entry = new DataSetEntry(count, className);
            this.dataSetEntries.add(entry);
            //Set up lists to keep xml attributes and values

            //Iterate over attribues  and populate columns and values list
            IntStream.range(0, attributes.getLength()).forEach((int i) -> {
                entry.addProperty(attributes.getLocalName(i), attributes.getValue(i));
                attrCounter++;
            });
        } catch (ClassNotFoundException ex) {
            if (attributes.getLength() != 2) {
                Logger.getLogger(SaxHandler.class.getName()).log(Level.WARNING, "Class "
                        + "not found for {0}.", className);
            } else {
                try {
                    Logger.getLogger(SaxHandler.class.getName()).log(Level.INFO, "Class "
                            + "not found for {0}. Checking to see if it is part of a"
                            + "many-to-many relationship", className);

                    String attr1 = attributes.getLocalName(0);
                    String attr2 = attributes.getLocalName(1);
                    if (attr1.endsWith("_id") && attr2.endsWith("_id")) {
                        //since the xml file has no idea who the owner is 
                        //we add the bidrectional relationship to both sides
                        //of the @ManyToMany relationship and let JPA figure it 
                        //out.
                        boolean found = addToManyToMany(attr1, attr2,
                                Integer.parseInt(attributes.getValue(0)),
                                Integer.parseInt(attributes.getValue(1)))
                                | addToManyToMany(attr2, attr1,
                                        Integer.parseInt(attributes.getValue(1)),
                                        Integer.parseInt(attributes.getValue(0)));
                        if (!found) {
                            Logger.getLogger(SaxHandler.class.getName()).log(Level.WARNING,
                                    "{0} with {1} & {2} not part of many-to-many "
                                    + "relationship", new Object[]{className, attr1,
                                        attr2});
                        } else {
                            Logger.getLogger(SaxHandler.class.getName()).log(Level.INFO,
                                    "{0} with {1} & {2} is part of many-to-many "
                                    + "relationship", new Object[]{className, attr1,
                                        attr2});
                        }
                    } else {
                        Logger.getLogger(SaxHandler.class.getName()).log(Level.WARNING,
                                "{0} with {1} attributes is not part of many-to-many"
                                + " relationship", new Object[]{className,
                                    attributes.getLength()});
                    }
                } catch (NumberFormatException exp) {
                    Logger.getLogger(SaxHandler.class.getName()).log(Level.WARNING,
                            "Error processing {0}", className);
                }
            }
        }
    }

    private boolean addToManyToMany(String owner, String owned,
            final Integer ownerId, final Integer ownedId) {
        owner = owner.substring(0, 1).toUpperCase() + owner.substring((1));
        owned = owned.substring(0, 1).toUpperCase() + owned.substring((1));
        String ownerClassName = modelPackageName + "." + owner.substring(0, owner.indexOf("_id"));
        String ownedClassName = modelPackageName + "." + owned.substring(0, owned.indexOf("_id"));
        try {
            Class owenedClazz = Class.forName(ownedClassName);
            Class owenerClazz = Class.forName(ownerClassName);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SaxHandler.class.getName()).log(Level.WARNING,
                    "At least one class for @ManyToMany not found: {0} | {1}",new Object[]{
                    ownerClassName,ownedClassName});
            return false;
        }
        //if (dataSetEnties.contains(ownerClassName) && dataSetEnties.contains(ownedClassName)) {
        return this.dataSetEntries.stream().anyMatch(de -> {
            if (de.getClazz().getName().equals(ownerClassName)
                    && Integer.parseInt(de.getValue("id")) == ownerId
                    && de.getManyToManyRelationships().get(ownedClassName) != null) {
                de.getManyToManyRelationships().
                        get(ownedClassName).add(ownedId);
                return true;
            } else {
                return false;
            }
        });
        //} else {
        //    return false;
        //}
    }

    @Override

    public
            void endDocument() throws SAXException {
        Logger.getLogger(SaxHandler.class
                .getName()).log(Level.INFO, "finished dataset {0} processing.", fileName);
    }

    @Override
    public
            void startDocument() throws SAXException {
        Logger.getLogger(SaxHandler.class
                .getName()).log(Level.INFO, "started dataset {0} processing.", fileName);
    }

    @Override
    public List<DataSetEntry> getDataEnties() {
        return this.dataSetEntries;
    }

    @Override
    public void process(String fileName, String modelPackageName) throws ParserException {
        this.fileName = fileName;
        this.modelPackageName = modelPackageName;
        this.dataSetEntries.clear();
        try {
            SAXParser parser;
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parser = parserFactory.newSAXParser();
            parser.parse(ClassLoader.getSystemResourceAsStream(fileName), this);

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(SaxHandler.class
                    .getName()).log(Level.SEVERE, "Error processing xml parser", ex);
            throw new ParserException(
                    "Error processing SAX XML parser", ex);
        }
    }

}
