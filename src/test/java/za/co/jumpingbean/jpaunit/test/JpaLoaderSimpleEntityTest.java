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
import za.co.jumpingbean.jpaunit.test.model.SimpleDateEntity;
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
    public void jpaSimpleStringEntityTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simplestringentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        em.clear();
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
    public void jpaSimpleDateEntityTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/simpledateentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        em.clear();
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
            qry.setParameter(1,cal.getTime());
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
        em.clear();
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
        em.clear();
        em.getTransaction().begin();
        try {
            Query qry = em.createQuery(
                    "Select s from SimpleIntegerEntity s");
            List<SimpleStringEntity> list = qry.getResultList();
            Assert.assertEquals("Failed to retrieve correct integerValue", 3, list.size());
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
        em.clear();
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
        em.clear();
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
            comp = new BigDecimal("1000");
            qry.setParameter(1, comp);
            ent = (SimpleBigDecimalEntity) qry.getSingleResult();
            Assert.assertNotNull("Should have found entity with a balance of 1000 - rounded up from 999.99999", ent);

            Assert.assertTrue(MessageFormat.format("Big Decimal should be {0} but was {1}", comp.intValue(), ent.getBigDecimalValue().intValue()),
                    ent.getBigDecimalValue().intValue() == comp.intValue());
        } finally {
            em.getTransaction().commit();
            loader.delete();
        }

    }

}
