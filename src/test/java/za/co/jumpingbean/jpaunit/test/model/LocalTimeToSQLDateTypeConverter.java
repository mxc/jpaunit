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

import java.sql.Date;
import java.time.LocalDate;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 *
 * @author mark
 */
@Converter(autoApply = true)
public class LocalTimeToSQLDateTypeConverter implements 
        AttributeConverter<LocalDate,Date> {

    @Override
    public java.sql.Date convertToDatabaseColumn(LocalDate date) {
        if (date==null) return null;
        return java.sql.Date.valueOf(date);
    }

    @Override
    public LocalDate convertToEntityAttribute(java.sql.Date sqlDate) {
        if (sqlDate==null) return null;
        return sqlDate.toLocalDate();
    }
}

