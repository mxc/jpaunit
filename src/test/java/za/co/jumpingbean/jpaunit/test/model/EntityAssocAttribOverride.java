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

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author mark
 */
@Entity
public class EntityAssocAttribOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id")
    private Integer id;

    @Embedded
    @AttributeOverride(name = "stringValue", column = @Column(name = "newStringValue"))
    @AssociationOverride(name="simpleLongEntity",joinColumns=@JoinColumn(name="simpleLongValue1_id"))
    private EmbeddableEnity embeddableEntity;

    @Embedded
    @AttributeOverrides(
            {
                @AttributeOverride(name = "intValue", column = @Column(name = "newInt")),
                @AttributeOverride(name = "stringValue", column = @Column(name = "newString")),
                @AttributeOverride(name = "localDateValue", column = @Column(name = "newLocalDate")),})
    @AssociationOverride(name = "simpleLongEntity", joinColumns = @JoinColumn(name = "simpleLongValue2_id"))
    private EmbeddableEnity embeddableEntity2;

    @ManyToOne
    @JoinColumn(name = "simpleStringEntity4_id")
    private SimpleStringEntity stringEntity4;

    @Column(name = "newStringValue4")
    private String stringValue4;

    public String getStringValue4() {
        return stringValue4;
    }

    public void setStringValue4(String stringValue4) {
        this.stringValue4 = stringValue4;
    }

    public EmbeddableEnity getEmbeddableEntity() {
        return embeddableEntity;
    }

    public void setEmbeddableEntity(EmbeddableEnity embeddableEntity) {
        this.embeddableEntity = embeddableEntity;
    }

    public SimpleStringEntity getStringEntity4() {
        return stringEntity4;
    }

    public void setStringEntity4(SimpleStringEntity stringEntity4) {
        this.stringEntity4 = stringEntity4;
    }

    public EmbeddableEnity getEmbeddableEntity2() {
        return embeddableEntity2;
    }

    public void setEmbeddableEntity2(EmbeddableEnity embeddableEntity2) {
        this.embeddableEntity2 = embeddableEntity2;
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}
