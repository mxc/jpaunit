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

    private final List<DataSetEntry> dataSetEnties = new ArrayList<>();
    private String modelPackageName;
    private final int count = 0;
    private String fileName;

    public SaxHandler() {
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        Logger.getLogger(SaxHandler.class.getName()).log(Level.INFO, "set {0} attribute values on {1}", new Object[]{count, qName});
        Logger.getLogger(SaxHandler.class.getName()).log(Level.INFO, "processed {0}", qName);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //ignore the root element
        if (qName.equalsIgnoreCase("dataset")) {
            return;
        }
        //Construct package name
        StringBuilder strBuf = new StringBuilder(modelPackageName);
        strBuf.append(".").append(qName);
        String className = strBuf.toString();
        try {
            DataSetEntry entry = new DataSetEntry(count, className);
            this.dataSetEnties.add(entry);
            //Set up lists to keep xml attributes and values
            //columns = new ArrayList<>();
            //values = new ArrayList<>();

            //count.set(0);
            //Iterate over attribues  and populate columns and values list
            IntStream.range(0, attributes.getLength()).forEach((int i) -> {
                entry.addProperty(attributes.getLocalName(i), attributes.getValue(i));
            });
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SaxHandler.class.getName()).log(Level.SEVERE, "Class not found for {0}", className);
        }

    }

    @Override

    public void endDocument() throws SAXException {
        Logger.getLogger(SaxHandler.class.getName()).log(Level.INFO, "finished dataset {0} processing.", fileName);
    }

    @Override
    public void startDocument() throws SAXException {
        Logger.getLogger(SaxHandler.class.getName()).log(Level.INFO, "started dataset {0} processing.", fileName);
    }

    @Override
    public List<DataSetEntry> getDataEnties() {
        return this.dataSetEnties;
    }

    @Override
    public void process(String fileName, String modelPackageName) throws ParserException {
        this.fileName = fileName;
        this.modelPackageName = modelPackageName;
        this.dataSetEnties.clear();
        try {
            SAXParser parser;
            SAXParserFactory parserFactor = SAXParserFactory.newInstance();
            parser = parserFactor.newSAXParser();
            parser.parse(ClassLoader.getSystemResourceAsStream(fileName), this);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(SaxHandler.class.getName()).log(Level.SEVERE, "Error processing xml parser", ex);
            throw new ParserException("Error processing SAX XML parser", ex);
        }
    }

}
