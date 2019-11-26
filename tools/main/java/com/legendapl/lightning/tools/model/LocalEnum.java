package com.legendapl.lightning.tools.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.legendapl.lightning.tools.common.Constants;

public enum LocalEnum {
    /**
     * 未定義
     */
    DEFAULST(0, "", Constants.P83_LOCAL_DEFAULT),

    EN(1, "en", Constants.P83_LOCAL_EN),

    FR(2, "fr", Constants.P83_LOCAL_FR),

    IT(3, "it", Constants.P83_LOCAL_IT),

    ES(4, "es", Constants.P83_LOCAL_ES),

    DE(5, "de", Constants.P83_LOCAL_DE),

    RO(6, "ro", Constants.P83_LOCAL_RO),
    
    JA(7, "ja", Constants.P83_LOCAL_JA),
    
    ZH_TW(8, "zh_TW", Constants.P83_LOCAL_ZH_TW),
    
    ZH_CN(9, "zh_CN", Constants.P83_LOCAL_ZH_CN);
    


    /**
     * 未定義のvalue
     */
    public static final int DEFAULST_VALUE = 0;

    public static final int EN_VALUE = 1;

    public static final int FR_VALUE = 2;

    public static final int IT_VALUE = 3;

    public static final int ES_VALUE = 4;

    public static final int DE_VALUE = 5;

    public static final int RO_VALUE = 6;
    
    public static final int JA_VALUE = 7;
    
    public static final int ZH_TW_VALUE = 8;
    
    public static final int ZH_CN_VALUE = 9;


    /**
     * すべての操作フラグ
     */
    private static final LocalEnum[] VALUES_ARRAY =
        new LocalEnum[] {
                DEFAULST,
                EN,
                FR,
                IT,
                ES,
                DE,
                RO,
                JA,
                ZH_TW,
                ZH_CN,
        };

    /**
     * すべての操作フラグ
     */
    public static final List<LocalEnum> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

    /**
     * 操作フラグ取得(by literal)
     */
    public static LocalEnum get(String literal) {
        for (int i = 0; i < VALUES_ARRAY.length; ++i) {
            LocalEnum result = VALUES_ARRAY[i];
            if (result.toString().equals(literal)) {
                return result;
            }
        }
        return null;
    }

    /**
     * 操作フラグ取得(by Name)
     */
    public static LocalEnum getByName(String name) {
        for (int i = 0; i < VALUES_ARRAY.length; ++i) {
            LocalEnum result = VALUES_ARRAY[i];
            if (result.getName().equals(name)) {
                return result;
            }
        }
        return null;
    }

    /**
     * 操作フラグ取得(by value)
     */
    public static LocalEnum get(int value) {
        switch (value) {
            case DEFAULST_VALUE: return DEFAULST;
            case EN_VALUE: return EN;
            case FR_VALUE: return FR;
            case IT_VALUE: return IT;
            case ES_VALUE: return ES;
            case DE_VALUE: return DE;
            case RO_VALUE: return RO;
            case JA_VALUE: return JA;
            case ZH_TW_VALUE: return ZH_TW;
            case ZH_CN_VALUE: return ZH_CN;
        }
        return null;
    }

    private final int value;

    private final String name;

    private final String literal;

    private LocalEnum(int value, String name, String literal) {
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
