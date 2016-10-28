package za.co.jumpingbean.jpaunit.test;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import za.co.jumpingbean.jpaunit.JpaLoader;
import za.co.jumpingbean.jpaunit.exception.ParserException;
import za.co.jumpingbean.jpaunit.loader.SaxHandler;
import za.co.jumpingbean.jpaunit.test.model.*;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;

public class JpaLoaderAnnotatedMethodEntityTest {

    private static EntityManager em;
    private final String modelPackageName = "za.co.jumpingbean.jpaunit.test.model";

    @BeforeClass
    public static void beforeClass() {
        em = Persistence.createEntityManagerFactory("jpaunittest").createEntityManager();
    }

    @Test
    public void jpaMethodCartoonEntityIdTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/namedcartoonwithnamedmethods.xml", modelPackageName, new SaxHandler(), em);
        loader.load();

        em.getTransaction().begin();
        try {

            Query qry = em.createQuery(
                    "Select s from NamedEntityWithNamedMethods s");
            List<NamedEntityWithNamedMethods> list = qry.getResultList();
            Assert.assertEquals("Should have loaded 2 NamedEntityWithNamedMethods objects", 2, list.size());
        } finally {
            em.getTransaction().commit();
            loader.delete();
        }
    }

    @Test
    public void jpaMethodStringEntityIdTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/methodstringidentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();

        em.getTransaction().begin();
        try {

            Query qry = em.createQuery(
                    "Select s from MethodStringIdEntity s");
            List<MethodStringIdEntity> list = qry.getResultList();
            Assert.assertEquals("Should have loaded 3 MethodStringIdEntity objects", 3, list.size());
        } finally {
            em.getTransaction().commit();
            loader.delete();
        }
    }

    @Test
    public void jpaMethodLongEntityTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/methodlongentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();

        em.getTransaction().begin();
        try {
            Query qry = em.createQuery(
                    "Select s from MethodLongEntity s");
            List<MethodLongEntity> list = qry.getResultList();
            Assert.assertEquals("Failed to retrieve correct longValue", 3, list.size());
            qry = em.createQuery("Select s from MethodLongEntity s where s.id =?");
            //Check null insert on long number range exceeded
            MethodLongEntity longE = new MethodLongEntity();
            longE.setId(3L);
            qry.setParameter(1, loader.lookupEntity(longE).getId());
            MethodLongEntity longE2 = (MethodLongEntity) qry.getSingleResult();
            Assert.assertNull(longE2.getLongValue());
        } finally {
            em.getTransaction().commit();
            loader.delete();
        }
    }

}
