package com.legendapl.lightning.tools.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import com.legendapl.lightning.tools.common.Constants;

public enum TimeZoneEnum {

    /**
     * 未定義
     */
    DEFAULT_TIMEZONE(0, TimeZone.getDefault().getID(), Constants.P83_DEFAULT_TIMEZONE),

    AMERICA_LOS_ANGELES(1, "America/Los_Angeles", Constants.P83_AMERICA_LOS_ANGELES),

    AMERICA_DENVER(2, "America/Denver", Constants.P83_AMERICA_DENVER),

    AMERICA_CHICAGO(3, "America/Chicago", Constants.P83_AMERICA_CHICAGO),

    AMERICA_NEW_YORK(4, "America/New_York", Constants.P83_AMERICA_NEW_YORK),

    EUROPE_LONDON(5, "Europe/London", Constants.P83_EUROPE_LONDON),

    EUROPE_BERLIN(6, "Europe/Berlin", Constants.P83_EUROPE_BERLIN),
    
    EUROPE_BUCHAREST(7, "Europe/Bucharest", Constants.P83_EUROPE_BUCHAREST);

    /**
     * 未定義のvalue
     */
    public static final int DEFAULT_TIMEZONE_VALUE = 0;

    public static final int AMERICA_LOS_ANGELES_VALUE = 1;

    public static final int AMERICA_DENVER_VALUE = 2;

    public static final int AMERICA_CHICAGO_VALUE = 3;

    public static final int AMERICA_NEW_YORK_VALUE = 4;

    public static final int EUROPE_LONDON_VALUE = 5;

    public static final int EUROPE_BERLIN_VALUE = 6;
    
    public static final int EUROPE_BUCHAREST_VALUE = 7;
    

    /**
     * すべての操作フラグ
     */
    private static final TimeZoneEnum[] VALUES_ARRAY =
        new TimeZoneEnum[] {
            DEFAULT_TIMEZONE,
            AMERICA_LOS_ANGELES,
            AMERICA_DENVER,
            AMERICA_CHICAGO,
            AMERICA_NEW_YORK,
            EUROPE_LONDON,
            EUROPE_BERLIN,
            EUROPE_BUCHAREST,
        };

    /**
     * すべての操作フラグ
     */
    public static final List<TimeZoneEnum> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

    /**
     * 操作フラグ取得(by literal)
     */
    public static TimeZoneEnum get(String literal) {
        for (int i = 0; i < VALUES_ARRAY.length; ++i) {
            TimeZoneEnum result = VALUES_ARRAY[i];
            if (result.toString().equals(literal)) {
                return result;
            }
        }
        return null;
    }

    /**
     * 操作フラグ取得(by Name)
     */
    public static TimeZoneEnum getByName(String name) {
        for (int i = 0; i < VALUES_ARRAY.length; ++i) {
            TimeZoneEnum result = VALUES_ARRAY[i];
            if (result.getName().equals(name)) {
                return result;
            }
        }
        return null;
    }

    /**
     * 操作フラグ取得(by value)
     */
    public static TimeZoneEnum get(int value) {
        switch (value) {
            case DEFAULT_TIMEZONE_VALUE: return DEFAULT_TIMEZONE;
            case AMERICA_LOS_ANGELES_VALUE: return AMERICA_LOS_ANGELES;
            case AMERICA_DENVER_VALUE: return AMERICA_DENVER;
            case AMERICA_CHICAGO_VALUE: return AMERICA_CHICAGO;
            case AMERICA_NEW_YORK_VALUE: return AMERICA_NEW_YORK;
            case EUROPE_LONDON_VALUE: return EUROPE_LONDON;
            case EUROPE_BERLIN_VALUE: return EUROPE_BERLIN;
            case EUROPE_BUCHAREST_VALUE: return EUROPE_BUCHAREST;
        }
        return null;
    }

    private final int value;

    private final String name;

    private final String literal;

    private TimeZoneEnum(int value, String name, String literal) {
        this.value = value;
        this.name = name;
        this.literal = literal;
    }

    public int getValue() {
      return value;
    }

    public String getName() {
      return name;
    }

    public String getLiteral() {
      return literal;
    }

    @Override
    public String toString() {
        return literal;
    }
}
