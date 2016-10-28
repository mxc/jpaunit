package za.co.jumpingbean.jpaunit.test;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import za.co.jumpingbean.jpaunit.JpaLoader;
import za.co.jumpingbean.jpaunit.exception.ParserException;
import za.co.jumpingbean.jpaunit.loader.SaxHandler;
import za.co.jumpingbean.jpaunit.test.model.NamedEntityWithNamedFields;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;

public class JpaLoaderAnnotatedFieldEntityTest {

    private static EntityManager em;
    private final String modelPackageName = "za.co.jumpingbean.jpaunit.test.model";

    @BeforeClass
    public static void beforeClass() {
        em = Persistence.createEntityManagerFactory("jpaunittest").createEntityManager();
    }

    @Test
    public void jpaMethodRobotEntityIdTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/namedrobotwithnamedfields.xml", modelPackageName, new SaxHandler(), em);
        loader.load();

        em.getTransaction().begin();
        try {

            Query qry = em.createQuery(
                    "Select s from NamedEntityWithNamedFields s");
            List<NamedEntityWithNamedFields> list = qry.getResultList();

            Assert.assertEquals("Should have loaded 2 NamedEntityWithNamedFields objects", 2, list.size());
        } finally {
            em.getTransaction().commit();
            loader.delete();
        }
    }
}
