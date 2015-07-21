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
package za.co.jumpingbean.jpaunit.fieldconverter;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import za.co.jumpingbean.jpaunit.exception.CannotConvertException;

/**
 *
 * @author mark
 */
public class DateFieldConverter implements FieldConverter<Date> {

    /**
     * The function expect to receive a date string in the following format
     * yyyy-mm-dd. This is only for the date in the dataset fed to the JPAUnit
     * loader
     *
     * @param elm
     * @return
     * @throws CannotConvertException
     */
    @Override
    public Date parse(String elm) throws CannotConvertException {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = fmt.parse(elm);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY,0);
            cal.set(Calendar.SECOND,0);
            cal.set(Calendar.MINUTE,0);
            cal.set(Calendar.MILLISECOND,0);
            return cal.getTime();
        } catch (ParseException ex) {
            Logger.getLogger(DateFieldConverter.class.getName()).log(Level.SEVERE, null, ex);
            throw new CannotConvertException(Date.class, MessageFormat
                    .format("Cannot convert date format {0} to date. "
                            + "Expected format yyyy-mm-dd", elm));
        }
    }

}
