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
package za.co.jumpingbean.jpaunit.test;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;
import za.co.jumpingbean.jpaunit.JpaLoader;
import za.co.jumpingbean.jpaunit.exception.ParserException;
import za.co.jumpingbean.jpaunit.loader.SaxHandler;
import za.co.jumpingbean.jpaunit.test.model.SimpleBigDecimalEntity;
import za.co.jumpingbean.jpaunit.test.model.SimpleBooleanEntity;
import za.co.jumpingbean.jpaunit.test.model.SimpleDateEntity;
import za.co.jumpingbean.jpaunit.test.model.SimpleIntegerEntity;
import za.co.jumpingbean.jpaunit.test.model.SimpleLocalDateEntity;
import za.co.jumpingbean.jpaunit.test.model.SimpleStringEntity;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author mark
 */
public class JpaLoaderSimpleEntityTest {

    private static EntityManager em;
    private final String modelPackageName = "za.co.jumpingbean.jpaunit.test.model";

    @BeforeClass
    public static void beforeClass() {
        em = Persistence.createEntityManagerFactory("jpaunittest").createEntityManager();
    }

    @Test
    public void JpaSimpleStringEntityTest() throws ParserException  {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simplestringentity.xml", modelPackageName,new SaxHandler(), em);
        loader.load();
        em.clear();
        em.getTransaction().begin();
        try {
            //FIXME: Throws no such field error huh?
//        Query qry = em.createQuery(
//                "Select s from SimpleStringEntity s");
//        List<SimpleStringEntity> list = qry.getResultList();
//        Assert.assertEquals("Should have loaded 3 SimpleStringEntity object", 3, list.size());
            SimpleStringEntity ent = em.find(SimpleStringEntity.class, 3);
            Assert.assertNotNull("Should have found entity with id 3", ent);
            Assert.assertEquals("Failed to retrieve correct stringValue", "test3", ent.getStringValue());
        } finally {
            em.getTransaction().commit();
        }
    }

    @Test
    public void JpaSimpleDateEntityTest() throws ParserException  {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simpledateentity.xml", modelPackageName,new SaxHandler(), em);
        loader.load();
        em.clear();
        em.getTransaction().begin();
        try {
            SimpleDateEntity ent = em.find(SimpleDateEntity.class, 3);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR,2018);
            cal.set(Calendar.MONTH,Calendar.JULY);
            cal.set(Calendar.DATE,6);
            cal.set(Calendar.HOUR_OF_DAY,0);
            cal.set(Calendar.MINUTE,0);
            cal.set(Calendar.SECOND,0);
            cal.set(Calendar.MILLISECOND,0);
            Assert.assertNotNull("Should have found entity with id 3", ent);
            Assert.assertTrue(MessageFormat.format("Date do not match. Expected {0} but was {1}",
                    cal.getTime(),ent.getDateValue()),cal.getTime().compareTo(ent.getDateValue())==0);
        } finally {
            em.getTransaction().commit();
        }
    }    
    
    
    @Test
    public void JpaSimpleLocalDateEntityTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simplelocaldateentity.xml", modelPackageName,new SaxHandler(), em);
        loader.load();
        em.clear();
        em.getTransaction().begin();
        try {
            LocalDate lDate = LocalDate.parse("2018-07-06",DateTimeFormatter.ISO_DATE);
            SimpleLocalDateEntity ent = em.find(SimpleLocalDateEntity.class, 3);
            Assert.assertNotNull("Should have found entity with id 3", ent);
            Assert.assertTrue(MessageFormat.format("Date do not match. Expected {0} but was {1}",lDate,ent.getLocalDate()),
                    lDate.compareTo(ent.getLocalDate())==0);
        } finally {
            em.getTransaction().commit();
        }
    }     
    
    
    @Test
    public void JpaSimpleIntegerEntityTest() throws ParserException  {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simpleintegerentity.xml", modelPackageName,new SaxHandler(), em);
        loader.load();
        em.clear();
        em.getTransaction().begin();
        try {
            SimpleIntegerEntity ent = em.find(SimpleIntegerEntity.class, 3);
            Assert.assertNotNull("Should have found entity with id 3", ent);
            Assert.assertEquals("Failed to retrieve correct integerValue", new Integer(1212), ent.getIntegerValue());
        } finally {
            em.getTransaction().commit();
        }
    }

    @Test
    public void JpaSimpleBooleanEntityTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simplebooleanentity.xml", modelPackageName, new SaxHandler(),em);
        loader.load();
        em.clear();
        em.getTransaction().begin();
        try {
            //FIXME: Throws no such field error huh?
//        Query qry = em.createQuery(
//                "Select s from SimpleBooleanEntity s");
//        List<SimpleStringEntity> list = qry.getResultList();
//        Assert.assertEquals("Should have loaded 4 SimpleBooleanEntity object", 4, list.size());
            SimpleBooleanEntity ent = em.find(SimpleBooleanEntity.class, 3);
            Assert.assertNotNull("Should have found entity with id 3", ent);
            Assert.assertTrue("Failed to retrieve correct booleanValue", ent.getBooleanValue());

            ent = em.find(SimpleBooleanEntity.class, 2);
            Assert.assertNotNull("Should have found entity with id 2", ent);
            Assert.assertFalse("Failed to retrieve correct booleanValue", ent.getBooleanValue());

            ent = em.find(SimpleBooleanEntity.class, 1);
            Assert.assertNotNull("Should have found entity with id 1", ent);
            Assert.assertFalse("Failed to retrieve correct booleanValue", ent.getBooleanValue());

            ent = em.find(SimpleBooleanEntity.class, 4);
            Assert.assertNotNull("Should have found entity with id 4", ent);
            Assert.assertTrue("Failed to retrieve correct booleanValue", ent.getBooleanValue());
        } finally {
            em.getTransaction().commit();
        }
    }

    @Test
    public void JpaSimpleBigDecimalEntityTest() throws ParserException  {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simplebigdecimalentity.xml", modelPackageName,new SaxHandler(), em);
        loader.load();
        em.clear();
        em.getTransaction().begin();
        try {
            SimpleBigDecimalEntity ent = em.find(SimpleBigDecimalEntity.class, 3);
            Assert.assertNotNull("Should have found entity with id 3", ent);
            BigDecimal comp =new BigDecimal("1024");
            Assert.assertTrue(MessageFormat.format("Big Decimal should be {0} but was {1}",comp,ent.getBigDecimalValue()),
                    ent.getBigDecimalValue().compareTo(comp)==0);
            
            ent = em.find(SimpleBigDecimalEntity.class, 1);
            Assert.assertNotNull("Should have found entity with id 1", ent);
            comp =new BigDecimal("1000.24");
            Assert.assertTrue(MessageFormat.format("Big Decimal should be {0} but was {1}",comp,ent.getBigDecimalValue()),
                    ent.getBigDecimalValue().compareTo(comp)==0);

            //SimpeBigDecimalEntity has been defined with a precision of 4 decimal places
            ent = em.find(SimpleBigDecimalEntity.class, 2);
            Assert.assertNotNull("Should have found entity with id 2", ent);
            comp =new BigDecimal("999999999999.9999");
            Assert.assertTrue(MessageFormat.format("Big Decimal should be {0} but was {1}",comp,ent.getBigDecimalValue()),
                    ent.getBigDecimalValue().compareTo(comp)==0);

            //SimpeBigDecimalEntity has been defined with a precision of 4 decimal places
            //The actual value set is 999.99999 -> this gets rounded to 1000
            ent = em.find(SimpleBigDecimalEntity.class, 4);
            Assert.assertNotNull("Should have found entity with id 4", ent);
            comp =new BigDecimal("1000");
            Assert.assertTrue(MessageFormat.format("Big Decimal should be {0} but was {1}",comp.intValue(),ent.getBigDecimalValue().intValue()),
                    ent.getBigDecimalValue().intValue() ==comp.intValue());            
        } finally {
            em.getTransaction().commit();
        }
    }    
    
}
