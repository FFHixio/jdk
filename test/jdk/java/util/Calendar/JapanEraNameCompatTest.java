/*
 * Copyright (c) 2019, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug 8218781
 * @summary Test the localized names of Japanese era Reiwa from COMPAT provider.
 * @modules jdk.localedata
 * @run testng/othervm -Djava.locale.providers=COMPAT JapanEraNameCompatTest
 */

import static java.util.Calendar.*;
import static java.util.Locale.*;

import java.time.LocalDate;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.JapaneseEra;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Locale;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

@Test
public class JapanEraNameCompatTest {
    static final Calendar c = new Calendar.Builder()
            .setCalendarType("japanese")
            .setFields(ERA, 5, YEAR, 1, MONTH, MAY, DAY_OF_MONTH, 1)
            .build();
    static final String EngName = "Reiwa";
    static final String CJName = "\u4ee4\u548c";
    static final String KoreanName = "\ub808\uc774\uc640";
    static final String ArabicName = "\u0631\u064a\u0648\u0627";
    static final String ThaiName = "\u0e40\u0e23\u0e27\u0e30";
    static final String HindiName = "\u0930\u0947\u0907\u0935\u093e";
    static final String RussianName = "\u0420\u044d\u0439\u0432\u0430";
    static final String SerbianName = "\u0420\u0435\u0438\u0432\u0430";
    static final String SerbLatinName = "Reiva";

    @DataProvider(name="UtilCalendar")
    Object[][] dataUtilCalendar() {
        return new Object[][] {
            //locale,   long,       short
            { JAPAN,    CJName,     "R" },
            { KOREAN,   KoreanName, "R" },
            { CHINA,    CJName,     "R" },
            { TAIWAN,   CJName,     "R" }, // fallback to zh
            { Locale.forLanguageTag("ar"), ArabicName, ArabicName },
            { Locale.forLanguageTag("th"), ThaiName, "R" },
            // hi_IN fallback to root
            { Locale.forLanguageTag("hi-IN"), EngName, "R" }
        };
    }

    @Test(dataProvider="UtilCalendar")
    public void testCalendarEraDisplayName(Locale locale,
            String longName, String shortName) {
        assertEquals(c.getDisplayName(ERA, LONG, locale), longName);
        assertEquals(c.getDisplayName(ERA, SHORT, locale), shortName);
    }

    @DataProvider(name="JavaTime")
    Object[][] dataJavaTime() {
        return new Object[][] {
            // locale, full, short
            { JAPAN, CJName, CJName },
            { KOREAN, KoreanName, KoreanName },
            { CHINA, CJName, CJName },
            { TAIWAN, CJName, CJName },
            { Locale.forLanguageTag("ar"), ArabicName, ArabicName },
            { Locale.forLanguageTag("th"), ThaiName, ThaiName },
            { Locale.forLanguageTag("hi-IN"), HindiName, HindiName },
            { Locale.forLanguageTag("ru"), RussianName, RussianName },
            { Locale.forLanguageTag("sr"), SerbianName, SerbianName },
            { Locale.forLanguageTag("sr-Latn"), SerbLatinName, SerbLatinName },
            { Locale.forLanguageTag("hr"), EngName, EngName },
            { Locale.forLanguageTag("in"), EngName, EngName },
            { Locale.forLanguageTag("lt"), EngName, EngName },
            { Locale.forLanguageTag("nl"), EngName, EngName },
            { Locale.forLanguageTag("no"), EngName, "R" },
            { Locale.forLanguageTag("sv"), EngName, EngName },
            // el fallback to root
            { Locale.forLanguageTag("el"), EngName, EngName }
        };
    }

    @Test(dataProvider="JavaTime")
    public void testChronoJapanEraDisplayName(Locale locale,
            String fullName, String shortName) {

        JapaneseEra era = JapaneseEra.valueOf("Reiwa");
        assertEquals(era.getDisplayName(TextStyle.FULL, locale), fullName);
        assertEquals(era.getDisplayName(TextStyle.SHORT, locale), shortName);
        assertEquals(era.getDisplayName(TextStyle.NARROW, locale), "R");
    }

    @Test
    public void testFormatParseEraName() {
        LocalDate date = LocalDate.of(2019, 5, 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd GGGG");
        formatter = formatter.withChronology(JapaneseChronology.INSTANCE);

        int num = 0;
        for (Locale locale : Calendar.getAvailableLocales()) {
            formatter = formatter.withLocale(locale);
            try {
                LocalDate.parse(date.format(formatter), formatter);
            } catch (DateTimeParseException e) {
                // If an array is defined for Japanese eras in java.time resource,
                // but an era entry is missing, format fallback to English name
                // while parse throw DateTimeParseException.
                num++;
                System.out.println("Missing java.time resource data for locale: " + locale);
            }
        }
        if (num > 0) {
            throw new RuntimeException("Missing java.time data for " + num + " locales");
        }
    }
}
