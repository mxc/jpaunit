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
package za.co.jumpingbean.jpaunit;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

/**
 *
 * @author mark
 */
public class DataSetEntry {

    private Integer entryIndex;
    private Class clazz;
    private final List<String> columns = new ArrayList<>();
    private final List<String> values = new ArrayList<>();
    //Create a lookup for attribute overrides
    private final Map<String, String> overrides = new HashMap<>();
    private final Map<String, List<Integer>> manyToManyRelationships = new HashMap<>();

    public DataSetEntry(Integer index, String clazz) throws ClassNotFoundException {
        this.clazz = Class.forName(clazz);
        this.entryIndex = index;
        this.updateOverrides();
    }

    private void updateOverrides() {
        //Process Class overrides, override, associationOverride and asssociationOverrides
        Class currentClass = clazz;
        while (currentClass.getSuperclass() != Object.class) {
            AttributeOverrides attributeOverrides[] = (AttributeOverrides[]) currentClass.getAnnotationsByType(AttributeOverrides.class);
            processOverridesArray(attributeOverrides);
            AttributeOverride[] tmpOverrides = (AttributeOverride[]) currentClass.getAnnotationsByType(AttributeOverride.class);
            processOverrideArray(tmpOverrides);
            currentClass = currentClass.getSuperclass();
            //Associations
            AssociationOverrides[] associationOverrides = (AssociationOverrides[]) currentClass.getAnnotationsByType(AssociationOverrides.class);
            processAssociationOverridesArray(associationOverrides);
            AssociationOverride[] associationOverride = (AssociationOverride[]) currentClass.getAnnotationsByType(AssociationOverride.class);
            processAssociationOverrideArray(associationOverride);
        }
        //Process field Overrides
        List<Field> fields = Utility.getAllFields(clazz);
        for (Field field : fields) {
            AttributeOverrides attributeOverridesArray[] = field.getAnnotationsByType(AttributeOverrides.class);
            processOverridesArray(attributeOverridesArray);
            AttributeOverride[] attributeOverrideArray = field.getAnnotationsByType(AttributeOverride.class);
            processOverrideArray(attributeOverrideArray);
            AssociationOverrides[] associationOverrides = (AssociationOverrides[]) field.getAnnotationsByType(AssociationOverrides.class);
            processAssociationOverridesArray(associationOverrides);
            AssociationOverride[] associationOverride = (AssociationOverride[]) field.getAnnotationsByType(AssociationOverride.class);
            processAssociationOverrideArray(associationOverride);
            JoinColumn[] joinColumns = field.getAnnotationsByType(JoinColumn.class);
            processJoinColumns(joinColumns, field.getName());
            Column[] columnsArray = field.getAnnotationsByType(Column.class);
            processColumns(columnsArray, field.getName());
            ManyToMany[] manyToManyAnnotations = field.getAnnotationsByType(ManyToMany.class);
            processManyToMany(manyToManyAnnotations, field);
        }
    }

    private void processJoinColumns(JoinColumn[] joinColumns, String originalName) {
        Arrays.stream(joinColumns).forEach(c -> {
            String tmpName = c.name();
            String newName = tmpName.substring(0, tmpName.length() - 3);
            if (!newName.equals(originalName)) {
                overrides.put(newName, originalName);
            }
        });
    }

    private void processColumns(Column[] columns, String originalName) {
        Arrays.stream(columns).forEach(c -> {
            String newName = c.name();
            if (!newName.equals(originalName)) {
                overrides.put(newName, originalName);
            }
        });
    }

    private void processAssociationOverridesArray(AssociationOverrides[] array) {
        for (AssociationOverrides os : array) {
            processAssociationOverrideArray(os.value());
        }
    }

    /**
     * Associations override names are the names of the database filed which by
     * convention for JPAUnit end in _id. We need to strip the _id out for the
     * lookup
     *
     * @param array
     */
    private void processAssociationOverrideArray(AssociationOverride[] array) {
        for (AssociationOverride o : array) {
            String orignalName = o.name();
            String tmpName = o.joinColumns()[0].name();
            String newName = tmpName.substring(0, tmpName.length() - 3);
            if (!newName.equals(orignalName)) {
                overrides.put(newName, orignalName);
            }
        }
    }

    private void processOverrideArray(AttributeOverride[] os) {
        for (AttributeOverride o : os) {
            String orignalName = o.name();
            String newName = o.column().name();
            if (!newName.equals(orignalName)) {
                overrides.put(newName, orignalName);
            }
        }
    }

    private void processOverridesArray(AttributeOverrides[] attributeOverrides) {
        for (AttributeOverrides os : attributeOverrides) {
            processOverrideArray(os.value());
        }
    }

    public void addToManyToManyCollection(String clazz, Integer id) {
        this.manyToManyRelationships.get(clazz).add(id);
    }

    public void addProperty(String column, String value) {
        //Check to see if the columns has had its name changed via @AttributeOverrides
        //replace with original property value.
        if (column.endsWith("_id")) {
            String tmpColumn = column.substring(0, column.length() - 3);
            if (overrides.containsKey(tmpColumn)) {
                column = overrides.get(tmpColumn) + "_id";
            }
        } else if (overrides.containsKey(column)) {
            column = overrides.get(column);
        }
        columns.add(column);
        values.add(value);
    }

    public List<String> getProperties() {
        return columns;
    }

    public Integer getIndexOfPropert(String propertyName) {
        return columns.indexOf(propertyName);
    }

    public String getValue(String column) {
        int index = columns.indexOf(column);
        if (index == -1) {
            Logger.getLogger(DataSetEntry.class.getName()).log(Level.WARNING, "{0} not found in columns list", column);
            return null;
        }
        return values.get(index);
    }

    public Integer getEntryIndex() {
        return entryIndex;
    }

    public void setEntryIndex(Integer index) {
        this.entryIndex = index;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        return "DataSetEntry{" + "entryIndex=" + entryIndex + ", clazz=" + clazz + ", columns=" + columns + ", values=" + values + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataSetEntry other = (DataSetEntry) obj;
        if (!Objects.equals(this.entryIndex, other.entryIndex)) {
            return false;
        }
        if (!Objects.equals(this.clazz, other.clazz)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.entryIndex);
        hash = 53 * hash + Objects.hashCode(this.clazz);
        return hash;
    }

    public void removeProperty(String property) {
        try {
            int indexOf = columns.indexOf(property);
            if (indexOf != -1) {
                columns.remove(indexOf);
                values.remove(indexOf);
            }
        } catch (IndexOutOfBoundsException ex) {
            Logger.getLogger(DataSetEntry.class.getName()).log(Level.SEVERE,
                    MessageFormat.format("Error removing property {0}", property), ex);
        }
    }

    //Create an entry for ManyToMany collections in owning class
    private void processManyToMany(ManyToMany[] manyToManyAnnotations, Field field) {
        Arrays.stream(manyToManyAnnotations).forEach(mtm -> {
            //only add the owning side of the manytomany relationship
            Type[] types = ((ParameterizedType) field.getGenericType())
                    .getActualTypeArguments();
            manyToManyRelationships.put(((Class)(types[0])).getName(),new LinkedList());
        });
    }

    public Map<String, List<Integer>> getManyToManyRelationships() {
        return manyToManyRelationships;
    }

}
