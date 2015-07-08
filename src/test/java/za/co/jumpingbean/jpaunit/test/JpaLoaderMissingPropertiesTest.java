/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.jumpingbean.jpaunit.test;

import java.math.BigDecimal;
import java.text.MessageFormat;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import za.co.jumpingbean.jpaunit.JpaLoader;
import za.co.jumpingbean.jpaunit.exception.ParserException;
import za.co.jumpingbean.jpaunit.loader.SaxHandler;
import za.co.jumpingbean.jpaunit.test.model.ForeignEntity;

/**
 *
 * @author mark
 */
public class JpaLoaderMissingPropertiesTest {

    private static EntityManager em;
    private final String modelPackageName = "za.co.jumpingbean.jpaunit.test.model";

    @BeforeClass
    public static void beforeClass() {
        em = Persistence.createEntityManagerFactory("jpaunittest").createEntityManager();
    }

    @Test
    public void embeddedTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/entitywithmissingproperty.xml", modelPackageName,
                new SaxHandler(), em);
        loader.load();
        em.clear();
        em.getTransaction().begin();
        try {
            ForeignEntity ent = em.find(ForeignEntity.class, 1);
            BigDecimal result = new BigDecimal("1000.24");
            Assert.assertTrue(MessageFormat.format("Expected {0} but got {1}",result,ent.getSimpleBigDecimal().getBigDecimalValue()),
            result.compareTo(ent.getSimpleBigDecimal().getBigDecimalValue())==0);
        } finally {
            em.getTransaction().commit();
        }
    }
}
