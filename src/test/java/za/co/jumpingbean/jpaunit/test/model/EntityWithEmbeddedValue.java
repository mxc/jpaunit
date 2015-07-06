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
package za.co.jumpingbean.jpaunit.test.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author mark
 */
@Entity
public class EntityWithEmbeddedValue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id")
    private Integer id; 
    
    private String stringValue1;
    private Boolean booleanValue;
    @Embedded
    private EmbeddableEnity embeddableEntity;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStringValue1() {
        return stringValue1;
    }

    public void setStringValue1(String stringValue1) {
        this.stringValue1 = stringValue1;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public EmbeddableEnity getEmbeddableEntity() {
        return embeddableEntity;
    }

    public void setEmbeddableEntity(EmbeddableEnity embeddableEntity) {
        this.embeddableEntity = embeddableEntity;
    }
    
    
    
}
