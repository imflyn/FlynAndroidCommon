package com.flyn.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Random;

public abstract class MathUtil
{
    public static double add(double v1, double v2)
    {
        DecimalFormat df = createDecimalFormat();
        BigDecimal b1 = new BigDecimal(df.format(v1));
        BigDecimal b2 = new BigDecimal(df.format(v2));
        return b1.add(b2).doubleValue();
    }

    private static DecimalFormat createDecimalFormat()
    {
        return new DecimalFormat("0.###############");
    }

    public static double sub(double v1, double v2)
    {
        DecimalFormat df = createDecimalFormat();
        BigDecimal b1 = new BigDecimal(df.format(v1));
        BigDecimal b2 = new BigDecimal(df.format(v2));
        return b1.subtract(b2).doubleValue();
    }

    public static double mul(double v1, double v2)
    {
        DecimalFormat df = createDecimalFormat();
        BigDecimal b1 = new BigDecimal(df.format(v1));
        BigDecimal b2 = new BigDecimal(df.format(v2));
        return b1.multiply(b2).doubleValue();
    }

    public static double div(double v1, double v2, int scale)
    {
        if (v2 == 0.0D)
            return 0.0D;
        if (scale < 0)
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        DecimalFormat df = createDecimalFormat();
        BigDecimal b1 = new BigDecimal(df.format(v1));
        BigDecimal b2 = new BigDecimal(df.format(v2));
        return b1.divide(b2, scale, 4).doubleValue();
    }

    public static double div(double v1, double v2)
    {
        if (v2 == 0.0D)
            return 0.0D;
        DecimalFormat df = createDecimalFormat();
        BigDecimal b1 = new BigDecimal(df.format(v1));
        BigDecimal b2 = new BigDecimal(df.format(v2));
        return b1.divide(b2, 4).doubleValue();
    }

    public static double round(double v, int scale)
    {
        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setGroupingUsed(false);
        formatter.setMaximumFractionDigits(scale);
        try
        {
            return formatter.parse(formatter.format(v)).doubleValue();
        } catch (ParseException e)
        {
            throw new RuntimeException(e);
        }

    }

    public static String round2(double v, int scale)
    {
        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setGroupingUsed(false);
        formatter.setMaximumFractionDigits(scale);
        formatter.setMinimumFractionDigits(scale);
        return formatter.format(v);
    }

    public static String thousandth(double v)
    {
        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setGroupingUsed(true);
        formatter.setMaximumFractionDigits(15);
        return formatter.format(v);
    }

    public static int Random(int range)
    {
        return new Random().nextInt(range);
    }
}