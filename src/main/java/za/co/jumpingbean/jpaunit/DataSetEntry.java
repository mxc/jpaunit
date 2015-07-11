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
package za.co.jumpingbean.jpaunit;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;

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

    public DataSetEntry(Integer index, String clazz) throws ClassNotFoundException {
        this.clazz = Class.forName(clazz);
        this.entryIndex = index;
        this.updateOverrides();
    }

    private void updateOverrides() {
        Class currentClass = clazz;
        while (currentClass.getSuperclass() != Object.class) {
            AttributeOverrides attributeOverrides[] = (AttributeOverrides[]) currentClass.getAnnotationsByType(AttributeOverrides.class);
            for (AttributeOverrides os : attributeOverrides) {
                for (AttributeOverride o : os.value()) {
                    String orignalName = o.name();
                    String overridenName = o.column().name();
                    overrides.put(overridenName, orignalName);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
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

}
