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
import java.util.logging.Level;
import java.util.logging.Logger;
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

/**
 *
 * @author mark
 */
public class JpaLoaderEntityDeletedDuringTest {

    private static EntityManager em;
    private final String modelPackageName = "za.co.jumpingbean.jpaunit.test.model";

    @BeforeClass
    public static void beforeClass() {
        em = Persistence.createEntityManagerFactory("jpaunittest").createEntityManager();
    }

    @Test
    public void dataSetEntityDeletedDuringRunTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/foreignentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        Integer id;
        em.getTransaction().begin();
        try {
            Query qry = em.createQuery("Select c from za.co.jumpingbean.jpaunit.test.model.ForeignEntity c");
            qry.setMaxResults(1);
            ForeignEntity ent = (ForeignEntity) qry.getSingleResult();
            id = ent.getId();
            Assert.assertNotNull("Expected object to be null",ent);
            em.remove(ent);
        }catch(Throwable ex){
            Logger.getLogger(JpaLoaderEntityDeletedDuringTest.class.getName()).log(Level.SEVERE,"Error testing delete of dataset entity during execution",ex);
            throw ex;
        } 
        finally {
            em.getTransaction().commit();
        }
        em.getTransaction().begin();
        ForeignEntity ent = em.find(ForeignEntity.class,id);
        Assert.assertNull(ent);
        em.getTransaction().commit();
        try{
        loader.delete();
        }catch (Throwable ex){
            Logger.getLogger(JpaLoaderEntityDeletedDuringTest.class.getName()).
                    log(Level.SEVERE,"There shouldn't be an error in dataset cleanup",ex);
        }
    }
    
   @Test
    public void dataSetEntityDeletedDuringRunUsingQueryTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/foreignentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        Integer id;
        em.getTransaction().begin();
        try {
            Query qry = em.createQuery("Select c from ForeignEntity c");
            qry.setMaxResults(1);
            ForeignEntity ent = (ForeignEntity) qry.getSingleResult();
            id = ent.getId();
            Assert.assertNotNull("Expected object to be null",ent);
            Query qry1 = em.createQuery("Delete from ForeignEntity c where c.id=?");
            qry1.setParameter(1,id);
            qry1.executeUpdate();
           
        }catch(Throwable ex){
            Logger.getLogger(JpaLoaderEntityDeletedDuringTest.class.getName()).
                    log(Level.SEVERE,"Error testing delete of dataset entity during execution",ex);
            throw ex;
        } 
        finally {
            em.getTransaction().commit();
        }
        em.getTransaction().begin();
        ForeignEntity ent = em.find(ForeignEntity.class,id);
        //Object is still in persistence context - delete via JQL does not
        //update the persistence context. Need to call em.clear() to clear
        //the cache. This is done in the delete method of JpaLoader.
        //This test is designs to test that it works.
        //User should call em.clear() themselves or make sure the cache is
        //synchronised.
        Assert.assertNotNull(ent);
        em.getTransaction().commit();
        try{
        loader.delete();
        }catch (Throwable ex){
            Logger.getLogger(JpaLoaderEntityDeletedDuringTest.class.getName()).log(Level.SEVERE,"There shouldn't be an error in dataset cleanup",ex);
        }
    }    
}
