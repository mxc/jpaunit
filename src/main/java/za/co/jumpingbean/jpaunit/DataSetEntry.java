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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mark
 */
public class DataSetEntry {

    private Integer entryIndex;
    private Class clazz;
    private final List<String> columns = new ArrayList<>();
    private final List<String> values = new ArrayList<>();
    
    public DataSetEntry(Integer index,String clazz) throws ClassNotFoundException{
        this.clazz=Class.forName(clazz);
        this.entryIndex=index;
    }

    public void addProperty(String column,String value){
        columns.add(column);
        values.add(value);
    }

    public List<String> getProperties(){
        return columns;
    }
    
    public Integer getIndexOfPropert(String propertyName){
        return columns.indexOf(propertyName);
    }
    
    public String getValue(String column){
        int index = columns.indexOf(column);
        if (index==-1) {
            Logger.getLogger(DataSetEntry.class.getName()).log(Level.WARNING,"{0} not found in columns list");
            return "";
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
        return "DataSetEntry{" + "index=" + entryIndex + ", clazz=" + clazz + '}';
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
        int indexOf = columns.indexOf(property);
        columns.remove(indexOf);
        values.remove(indexOf);
    }

}
