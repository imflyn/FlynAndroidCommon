package com.greatwall.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class Cn2Spell
{

    /**
     * 汉字转换位汉语拼音首字母，英文字符不变
     * 
     * @param chines
     *            汉字
     * @return 拼音
     */
    private HanyuPinyinOutputFormat defaultFormat;

    public Cn2Spell()
    {
        if (null == defaultFormat)
        {
            this.defaultFormat = new HanyuPinyinOutputFormat();
            defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
            defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        }
    }

    public String converterToFirstSpell(String chines)
    {
        StringBuilder pinyinName = new StringBuilder();
        char[] nameChar = chines.toCharArray();
        for (int i = 0; i < nameChar.length; i++)
        {
            if (nameChar[i] > 128)
            {
                try
                {
                    pinyinName.append(PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0].charAt(0));
                } catch (BadHanyuPinyinOutputFormatCombination e)
                {
                    e.printStackTrace();
                }
            } else
            {
                pinyinName.append(nameChar[i]);
            }
        }
        return pinyinName.toString();
    }

    /**
     * 汉字转换位汉语拼音，英文字符不变
     * 
     * @param chines
     *            汉字
     * @return 拼音
     */
    public String converterToSpell(String chines)
    {
        StringBuilder pinyinName = new StringBuilder();
        char[] nameChar = chines.toCharArray();
        for (int i = 0; i < nameChar.length; i++)
        {
            if (nameChar[i] > 128)
            {
                try
                {
                    pinyinName.append(PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0]);
                } catch (BadHanyuPinyinOutputFormatCombination e)
                {
                    e.printStackTrace();
                }
            } else
            {
                pinyinName.append(nameChar[i]);
            }
        }
        return pinyinName.toString();
    }

}