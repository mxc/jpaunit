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

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import za.co.jumpingbean.jpaunit.JpaLoader;
import za.co.jumpingbean.jpaunit.exception.ParserException;
import za.co.jumpingbean.jpaunit.loader.SaxHandler;
import za.co.jumpingbean.jpaunit.test.model.SimpleBigDecimalEntity;
import za.co.jumpingbean.jpaunit.test.model.SimpleByteEntity;
import za.co.jumpingbean.jpaunit.test.model.SimpleDateEntity;
import za.co.jumpingbean.jpaunit.test.model.SimpleDoubleEntity;
import za.co.jumpingbean.jpaunit.test.model.SimpleFloatEntity;
import za.co.jumpingbean.jpaunit.test.model.SimpleIntegerEntity;
import za.co.jumpingbean.jpaunit.test.model.SimpleLocalDateEntity;
import za.co.jumpingbean.jpaunit.test.model.SimpleLongEntity;
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
    public void jpaSimpleStringEntityTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simplestringentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        //em.clear();
        em.getTransaction().begin();
        try {

            Query qry = em.createQuery(
                    "Select s from SimpleStringEntity s");
            List<SimpleStringEntity> list = qry.getResultList();
            Assert.assertEquals("Should have loaded 3 SimpleIntegerEntity object", 3, list.size());
        } finally {
            em.getTransaction().commit();
            loader.delete();
        }
    }

    @Test
    public void jpaSimpleStringEntityIdTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simplestringidentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        //em.clear();
        em.getTransaction().begin();
        try {

            Query qry = em.createQuery(
                    "Select s from SimpleStringIdEntity s");
            List<SimpleStringEntity> list = qry.getResultList();
            Assert.assertEquals("Should have loaded 3 SimpleStringIdEntity objects", 3, list.size());
        } finally {
            em.getTransaction().commit();
            loader.delete();
        }
    }

    @Test
    public void jpaSimpleDateEntityTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simpledateentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        //em.clear();
        em.getTransaction().begin();
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, 2018);
            cal.set(Calendar.MONTH, Calendar.JULY);
            cal.set(Calendar.DATE, 6);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Query qry = em.createQuery("Select c from SimpleDateEntity c where c.dateValue=?");
            qry.setParameter(1, cal.getTime());
            SimpleDateEntity ent = (SimpleDateEntity) qry.getSingleResult();
            Assert.assertNotNull("Should have found entity with id 3", ent);
            Assert.assertTrue(MessageFormat.format("Date do not match. Expected {0} but was {1}",
                    cal.getTime(), ent.getDateValue()), cal.getTime().compareTo(ent.getDateValue()) == 0);
        } finally {
            em.getTransaction().commit();
            loader.delete();
        }

    }

    @Test
    public void jpaSimpleLocalDateEntityTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simplelocaldateentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        //em.clear();
        em.getTransaction().begin();
        try {
            LocalDate lDate = LocalDate.parse("2018-07-06", DateTimeFormatter.ISO_DATE);
            Query qry = em.createQuery("Select s from SimpleLocalDateEntity s where s.localDate=?");
            qry.setParameter(1, lDate);
            SimpleLocalDateEntity ent = (SimpleLocalDateEntity) qry.getSingleResult();
            Assert.assertNotNull("Should have found entity with local date of 2018-07-06", ent);
            Assert.assertTrue(MessageFormat.format("Date do not match. Expected {0} but was {1}", lDate, ent.getLocalDate()),
                    lDate.compareTo(ent.getLocalDate()) == 0);
        } finally {
            em.getTransaction().commit();
            loader.delete();
        }

    }

    @Test
    public void jpaSimpleIntegerEntityTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simpleintegerentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        //em.clear();
        em.getTransaction().begin();
        try {
            Query qry = em.createQuery(
                    "Select s from SimpleIntegerEntity s");
            List<SimpleStringEntity> list = qry.getResultList();
            Assert.assertEquals("Failed to retrieve correct integerValue", 3, list.size());

            //Check null insert on long number range exceeded
            qry = em.createQuery("Select s from SimpleIntegerEntity s where s.id =?");
            SimpleIntegerEntity tmpIntegere = new SimpleIntegerEntity();
            tmpIntegere.setId(3);
            qry.setParameter(1, loader.lookupEntity(tmpIntegere).getId());
            SimpleIntegerEntity integeree = (SimpleIntegerEntity) qry.getSingleResult();
            Assert.assertNull(integeree.getIntegerValue());

        } finally {
            em.getTransaction().commit();
            loader.delete();
        }
    }

    @Test
    public void jpaSimpleBooleanEntityTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simplebooleanentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        //em.clear();
        em.getTransaction().begin();
        try {
            Query qry = em.createQuery(
                    "Select s from SimpleBooleanEntity s where booleanValue =?");
            qry.setParameter(1, true);
            List<SimpleStringEntity> list = qry.getResultList();
            Assert.assertEquals("Should have loaded 2 SimpleBooleanEntity objects with true", 2, list.size());

            qry.setParameter(1, false);
            list = qry.getResultList();
            Assert.assertEquals("Should have loaded 2 SimpleBooleanEntity with false", 2, list.size());

        } finally {
            em.getTransaction().commit();
            loader.delete();
        }
    }

    @Test
    public void jpaSimpleBigDecimalEntityTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simplebigdecimalentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        //em.clear();
        em.getTransaction().begin();
        try {
            Query qry = em.createQuery("Select c from SimpleBigDecimalEntity c where c.bigDecimalValue=?");
            BigDecimal dec = new BigDecimal("1024");
            qry.setParameter(1, dec);
            SimpleBigDecimalEntity ent = (SimpleBigDecimalEntity) qry.getSingleResult();
            Assert.assertNotNull("Should have found entity with id balance 1024", ent);

            BigDecimal comp = new BigDecimal("1024");
            Assert.assertTrue(MessageFormat.format("Big Decimal should be {0} but was {1}", comp, ent.getBigDecimalValue()),
                    ent.getBigDecimalValue().compareTo(comp) == 0);

            comp = new BigDecimal("1000.24");
            qry.setParameter(1, comp);
            ent = (SimpleBigDecimalEntity) qry.getSingleResult();
            Assert.assertNotNull("Should have found entity with balance 1000.24", ent);

            Assert.assertTrue(MessageFormat.format("Big Decimal should be {0} but was {1}", comp, ent.getBigDecimalValue()),
                    ent.getBigDecimalValue().compareTo(comp) == 0);

            //SimpeBigDecimalEntity has been defined with a precision of 4 decimal places
            comp = new BigDecimal("999999999999.9999");
            qry.setParameter(1, comp);
            ent = (SimpleBigDecimalEntity) qry.getSingleResult();
            Assert.assertNotNull("Should have found entity with balance 999999999999.9999", ent);

            Assert.assertTrue(MessageFormat.format("Big Decimal should be {0} but was {1}", comp, ent.getBigDecimalValue()),
                    ent.getBigDecimalValue().compareTo(comp) == 0);

            //SimpeBigDecimalEntity has been defined with a precision of 4 decimal places
            //The actual value set is 999.99999 -> this gets rounded to 1000
            SimpleBigDecimalEntity bigDecimaleEntity = new SimpleBigDecimalEntity();
            bigDecimaleEntity.setId(4);
            SimpleBigDecimalEntity actual = loader.lookupEntity(bigDecimaleEntity);
            //Test rounding of BigDecimal values with precision greater than entity
            Query qry2 = em.createQuery("Select b from SimpleBigDecimalEntity b where b.id =?");
            qry2.setParameter(1, actual.getId());
            ent = (SimpleBigDecimalEntity) qry2.getSingleResult();
            comp = new BigDecimal("1000");
            System.out.println(ent.getBigDecimalValue());
            Assert.assertTrue(MessageFormat.format("Big Decimal should be {0} but was {1} difference greater than 1", comp.intValue(), ent.getBigDecimalValue().intValue()), 
                    comp.subtract(ent.getBigDecimalValue()).abs().compareTo(BigDecimal.ONE)<0);
        } finally {
            em.getTransaction().commit();
            loader.delete();
        }

    }

    @Test
    public void jpaSimpleLongEntityTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simplelongentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        //em.clear();
        em.getTransaction().begin();
        try {
            Query qry = em.createQuery(
                    "Select s from SimpleLongEntity s");
            List<SimpleLongEntity> list = qry.getResultList();
            Assert.assertEquals("Failed to retrieve correct longValue", 3, list.size());
            qry = em.createQuery("Select s from SimpleLongEntity s where s.id =?");
            //Check null insert on long number range exceeded
            SimpleLongEntity longE = new SimpleLongEntity();
            longE.setId(3);
            qry.setParameter(1, loader.lookupEntity(longE).getId());
            SimpleLongEntity longE2 = (SimpleLongEntity) qry.getSingleResult();
            Assert.assertNull(longE2.getLongValue());
        } finally {
            em.getTransaction().commit();
            loader.delete();
        }
    }

    @Test
    public void jpaSimpleDoubleEntityTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simpledoubleentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        //em.clear();
        em.getTransaction().begin();
        try {
            Query qry = em.createQuery(
                    "Select s from SimpleDoubleEntity s");
            List<SimpleStringEntity> list = qry.getResultList();
            Assert.assertEquals("Failed to retrieve correct doubleValue", 3, list.size());
            qry = em.createQuery("Select s from SimpleDoubleEntity s where s.id =?");
            //Check null insert on long number range exceeded
            SimpleDoubleEntity doublee = new SimpleDoubleEntity();
            doublee.setId(3);
            qry.setParameter(1, loader.lookupEntity(doublee).getId());
            SimpleDoubleEntity doublee2 = (SimpleDoubleEntity) qry.getSingleResult();
            Assert.assertEquals((double) Double.parseDouble("92233720368547758086786785689789765867867868657856865868658568686867585.5645656456463353434"),
                    (double) doublee2.getDoubleValue(), 0);
        } finally {
            em.getTransaction().commit();
            loader.delete();
        }
    }

    @Test
    public void jpaSimpleFloatEntityTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simplefloatentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        //em.clear();
        em.getTransaction().begin();
        try {
            Query qry = em.createQuery(
                    "Select s from SimpleFloatEntity s");
            List<SimpleStringEntity> list = qry.getResultList();
            Assert.assertEquals("Failed to retrieve correct floatValue", 3, list.size());
            qry = em.createQuery("Select s from SimpleFloatEntity s where s.id =?");
            //Check null insert on long number range exceeded
            SimpleFloatEntity floate = new SimpleFloatEntity();
            floate.setId(3);
            qry.setParameter(1, loader.lookupEntity(floate).getId());
            SimpleFloatEntity floatee2 = (SimpleFloatEntity) qry.getSingleResult();
            Assert.assertNull(floatee2.getFloatValue());
        } finally {
            em.getTransaction().commit();
            loader.delete();
        }
    }

    @Test
    public void jpaSimpleByteEntityTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simplebyteentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        //em.clear();
        em.getTransaction().begin();
        try {
            Query qry = em.createQuery(
                    "Select s from SimpleByteEntity s");
            List<SimpleStringEntity> list = qry.getResultList();
            Assert.assertEquals("Failed to retrieve correct byteValue", 3, list.size());

            //Check null insert on long number range exceeded
            qry = em.createQuery("Select s from SimpleByteEntity s where s.id =?");
            SimpleByteEntity tmpByte = new SimpleByteEntity();
            tmpByte.setId(3);
            qry.setParameter(1, loader.lookupEntity(tmpByte).getId());
            SimpleByteEntity bytee = (SimpleByteEntity) qry.getSingleResult();
            Assert.assertNull(bytee.getByteValue());

        } finally {
            em.getTransaction().commit();
            loader.delete();
        }
    }

}
