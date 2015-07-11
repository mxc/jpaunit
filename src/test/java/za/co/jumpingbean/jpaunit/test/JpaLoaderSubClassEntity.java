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
import javax.persistence.Query;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import za.co.jumpingbean.jpaunit.JpaLoader;
import za.co.jumpingbean.jpaunit.exception.ParserException;
import za.co.jumpingbean.jpaunit.loader.SaxHandler;
import za.co.jumpingbean.jpaunit.test.model.ForeignEntity;
import za.co.jumpingbean.jpaunit.test.model.SubClassEntity;

/**
 *
 * @author mark
 */
public class JpaLoaderSubClassEntity {

    private static EntityManager em;
    private final String modelPackageName = "za.co.jumpingbean.jpaunit.test.model";

    @BeforeClass
    public static void beforeClass() {
        em = Persistence.createEntityManagerFactory("jpaunittest").createEntityManager();
    }

    @Test
    public void embeddedTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/subclassentity.xml", modelPackageName,
                new SaxHandler(), em);
        loader.load();
        em.clear();
        em.getTransaction().begin();
        try {
            String result = "test1";
            Query qry = em.createQuery("Select c from SubClassEntity c where c.stringValue=?");
            qry.setParameter(1, result);
            SubClassEntity ent = (SubClassEntity) qry.getSingleResult();
            Assert.assertEquals(MessageFormat.format("Expected test1 but got {1}",ent.getStringValue()),"test1",ent.getStringValue());
        } finally {
            em.getTransaction().commit();
            loader.delete();
        }

    }
}
