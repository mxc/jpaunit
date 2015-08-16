/*
 * Copyright (C) 2015 Mark Clarke.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package za.co.jumpingbean.jpaunit.test;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import za.co.jumpingbean.jpaunit.JpaLoader;
import za.co.jumpingbean.jpaunit.exception.ParserException;
import za.co.jumpingbean.jpaunit.loader.SaxHandler;
import za.co.jumpingbean.jpaunit.test.model.OwnedEntity;
import za.co.jumpingbean.jpaunit.test.model.OwnerEntity;

/**
 *
 * @author Mark Clarke
 */
public class JpaLoaderManyToManyTest {

    private static EntityManager em;
    private final String modelPackageName = "za.co.jumpingbean.jpaunit.test.model";

    @BeforeClass
    public static void beforeClass() {
        em = Persistence.createEntityManagerFactory("jpaunittest").createEntityManager();
    }

    @Test
    public void loadManyToManyTest() throws ParserException {
        JpaLoader loader = new JpaLoader();
        loader.init("META-INF/manytomany.xml", modelPackageName, new SaxHandler(), em);
        loader.load();
        if (em.getTransaction().isActive()) {
            em.getTransaction().commit();
        }
        em.clear();

        em.getTransaction().begin();
            OwnerEntity o1 = new OwnerEntity();
            o1.setId(1);
            o1 = loader.lookupEntity(o1);

            OwnerEntity o2 = new OwnerEntity();
            o2.setId(2);
            o2 = loader.lookupEntity(o2);

            TypedQuery<OwnerEntity> oq = em.createQuery(
                    "Select o from OwnerEntity o where o.id = ?", OwnerEntity.class);
            oq.setParameter(1, o1.getId());
            List<OwnerEntity> r = oq.getResultList();
            Assert.assertEquals(2, r.get(0).getOwned().size());

            oq.setParameter(1, o2.getId());
            r = oq.getResultList();
            Assert.assertEquals(1, r.get(0).getOwned().size());
            
            
            OwnedEntity od = new OwnedEntity();
            od.setId(2);
            od = loader.lookupEntity(od);
            TypedQuery<OwnedEntity> odq = em.createQuery(
                    "Select o from OwnedEntity o where o.id = ?", OwnedEntity.class);
            odq.setParameter(1, od.getId());
            List<OwnedEntity> r2 = odq.getResultList();
            Assert.assertEquals(2, r2.get(0).getOwnerEntities().size());            
    }

}
