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
public class JpaLoaderForeignEntityTest {

    private static EntityManager em;
    private final String modelPackageName = "za.co.jumpingbean.jpaunit.test.model";

    @BeforeClass
    public static void beforeClass() {
        em = Persistence.createEntityManagerFactory("jpaunittest").createEntityManager();
    }

    @Test
    public void foreignTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/foreignentity.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        //em.clear();
        em.getTransaction().begin();
        try {
            Query qry = em.createQuery("Select c from ForeignEntity c where c.stringValue=?");
            qry.setParameter(1,"String Value");
            ForeignEntity ent = (ForeignEntity) qry.getSingleResult();
            BigDecimal result = new BigDecimal("1000.24");
            Assert.assertTrue(MessageFormat.format("Expected {0} but got {1}",result,ent.getSimpleBigDecimal().getBigDecimalValue()),
            result.compareTo(ent.getSimpleBigDecimal().getBigDecimalValue())==0);
        } finally {
            em.getTransaction().commit();
            loader.delete();
        }
    }

}
