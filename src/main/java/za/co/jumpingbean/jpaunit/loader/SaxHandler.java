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
                //Convert first letter of attribute name to uppercase so it matches
                //standard naming convention for Java classes
                StringBuilder strBufName = new StringBuilder(attributes.getLocalName(i));
                strBufName.setCharAt(0, Character.toUpperCase(strBufName.charAt(0)));
                String strName = strBufName.toString();
                entry.addProperty(strName, attributes.getValue(i));
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
    public void process(String fileName,String modelPackageName) throws ParserException {
        this.fileName = fileName;
        this.modelPackageName=modelPackageName;
        try {
            SAXParser parser;
            SAXParserFactory parserFactor = SAXParserFactory.newInstance();
            parser = parserFactor.newSAXParser();
            parser.parse(ClassLoader.getSystemResourceAsStream(fileName), this);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(SaxHandler.class.getName()).log(Level.SEVERE, "Error processing xml parser", ex);
            throw new ParserException("Error processing SAX XML parser");
        }
    }

}
