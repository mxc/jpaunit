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
import za.co.jumpingbean.jpaunit.test.model.EntityWithEnum;
import za.co.jumpingbean.jpaunit.test.model.ForeignEntity;
import za.co.jumpingbean.jpaunit.test.model.NewEnum;

/**
 *
 * @author mark
 */
public class JpaLoaderEntityWithEnum {

    private static EntityManager em;
    private final String modelPackageName = "za.co.jumpingbean.jpaunit.test.model";

    @BeforeClass
    public static void beforeClass() {
        em = Persistence.createEntityManagerFactory("jpaunittest").createEntityManager();
    }

    @Test
    public void ForeginTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/entitywithenum.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        em.clear();
        em.getTransaction().begin();
        try {
            Query qry = em.createQuery("Select c from EntityWithEnum c where c.enumValue=?");
            qry.setParameter(1,NewEnum.UBUNTU);
            List<EntityWithEnum> ent =  qry.getResultList();
            Assert.assertEquals(1, ent.size());
        } finally {
            em.getTransaction().commit();
            loader.delete();
        }
    }

}
