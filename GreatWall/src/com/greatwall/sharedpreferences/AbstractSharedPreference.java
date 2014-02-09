package com.greatwall.sharedpreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;

import com.greatwall.util.PackageUtil;

public abstract class AbstractSharedPreference
{

    private static final String TAG                 = AbstractSharedPreference.class.getName();
    private static final String DEFAULT_STORE_NAME  = "SharedPreference";
    private static final String DEFAULT_KEY_VERSION = "1.0.0";

    private Context             context;
    private String              xmlFileName;
    private SharedPreferences   sharedPreferences;

    public String getXmlFileName()
    {
        return xmlFileName;
    }

    public AbstractSharedPreference(Context context, String storeName)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("context can not be null");
        }
        if (storeName == null || storeName.length() <= 0)
        {
            storeName = DEFAULT_STORE_NAME;
        }
        this.context = context;
        this.xmlFileName = buildSharedPreferencesFileName(this.context, storeName);
        this.sharedPreferences = context.getSharedPreferences(this.xmlFileName, Context.MODE_PRIVATE);
    }

    private String buildSharedPreferencesFileName(Context context, String xmlFileName)
    {
        // 把service注册成运行在单独的进程，因此Application的onCreate()会被调用多次，所以不同的进程绝对不能操作同一个文件！
        String pName = PackageUtil.getCurrentProcessName(context);
        if (pName != null)
        {
            pName = pName.replaceAll("[^\\w]+", "_");
        }
        return xmlFileName + "_" + pName;
    }

    /**
     * 存一个值<br>
     * value的类型仅支持int/Integer,boolean/Boolean,String,long/Long,float/Float
     * 
     * @param key
     *            key
     * @param value
     *            值 @
     */
    public void write(String key, Object value)
    {
        write(DEFAULT_KEY_VERSION, key, value);
    }

    /**
     * 存一个值<br>
     * value的类型仅支持int/Integer,boolean/Boolean,String,long/Long,float/Float
     * 
     * @param version
     *            key版本
     * @param key
     *            key
     * @param value
     *            值
     */
    public void write(String version, String key, Object value)
    {
        if (version == null || version.length() <= 0 || key == null || key.length() <= 0 || value == null)
        {
            return;
        }
        try
        {
            Editor editor = sharedPreferences.edit();
            writeToEditor(key, value, editor, version);
            apply(editor);
        } catch (Throwable e)
        {
            Log.w(TAG, "write SharedPreferences error,key:" + key + ",value:" + value + ",xmlFileName:" + getXmlFileName());
        }
    }

    /**
     * 批量保存<br>
     * value的类型仅支持int/Integer,boolean/Boolean,String,long/Long,float/Float
     * 
     * @param map
     */
    public void batchWrite(HashMap<String, Object> map)
    {
        batchWrite(DEFAULT_KEY_VERSION, map);
    }

    /**
     * 批量保存<br>
     * value的类型仅支持int/Integer,boolean/Boolean,String,long/Long,float/Float
     * 
     * @param map
     */
    public void batchWrite(String version, HashMap<String, Object> map)
    {
        if (version == null || version.length() <= 0)
        {
            return;
        }
        if (map == null || map.size() <= 0)
        {
            return;
        }
        try
        {
            Editor editor = sharedPreferences.edit();
            for (Map.Entry<String, Object> entry : map.entrySet())
            {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (key == null || key.length() <= 0 || value == null)
                {
                    continue;
                }
                writeToEditor(key, value, editor, version);
            }
            apply(editor);
        } catch (Throwable e)
        {
            Log.w(TAG, "write SharedPreferences error,map:" + map + ",xmlFileName:" + getXmlFileName());
        }
    }

    private void writeToEditor(String key, Object value, Editor editor, String version)
    {
        key = version + "|" + key;
        Class<?> clazz = value.getClass();
        if (clazz == Integer.class || clazz == int.class)
        {
            editor.putInt(key, (Integer) value);
        } else if (clazz == String.class)
        {
            editor.putString(key, (String) value);
        } else if (clazz == Boolean.class || clazz == boolean.class)
        {
            editor.putBoolean(key, (Boolean) value);
        } else if (clazz == Long.class || clazz == long.class)
        {
            editor.putLong(key, (Long) value);
        } else if (clazz == Float.class || clazz == float.class)
        {
            editor.putFloat(key, (Float) value);
        } else
        {
            Log.w(TAG, "unsupported class type:" + clazz + ",key:" + key + ",value:" + value + ",xmlFileName:" + getXmlFileName());
        }
    }

    /**
     * 取一个String
     * 
     * @param key
     *            key
     * @param defaultvalue
     *            默认值
     * @return key对应的String
     */
    public String read(String key, String defaultvalue)
    {
        return read(DEFAULT_KEY_VERSION, key, defaultvalue);
    }

    /**
     * 取一个String
     * 
     * @param version
     *            key版本
     * @param key
     *            key
     * @param defaultvalue
     *            默认值
     * @return key对应的String
     */
    public String read(String version, String key, String defaultvalue)
    {
        if (key == null || key.length() <= 0)
        {
            return defaultvalue;
        }
        if (version == null || version.length() <= 0)
        {
            version = DEFAULT_KEY_VERSION;
        }
        return sharedPreferences.getString(version + "|" + key, defaultvalue);
    }

    /**
     * 取一个int
     * 
     * @param key
     *            key
     * @param defaultvalue
     *            默认值
     * @return key对应的int
     */
    public int read(String key, int defaultvalue)
    {
        return read(DEFAULT_KEY_VERSION, key, defaultvalue);
    }

    /**
     * 取一个int
     * 
     * @param version
     *            key版本
     * @param key
     *            key
     * @param defaultvalue
     *            默认值
     * @return key对应的int
     */
    public int read(String version, String key, int defaultvalue)
    {
        if (key == null || key.length() <= 0)
        {
            return defaultvalue;
        }
        if (version == null || version.length() <= 0)
        {
            version = DEFAULT_KEY_VERSION;
        }
        return sharedPreferences.getInt(version + "|" + key, defaultvalue);
    }

    /**
     * 取一个long
     * 
     * @param key
     *            key
     * @param defaultvalue
     *            默认值
     * @return key对应的long
     */
    public long read(String key, long defaultvalue)
    {
        return read(DEFAULT_KEY_VERSION, key, defaultvalue);
    }

    /**
     * 取一个long
     * 
     * @param version
     *            key版本
     * @param key
     *            key
     * @param defaultvalue
     *            默认值
     * @return key对应的long
     */
    public long read(String version, String key, long defaultvalue)
    {
        if (key == null || key.length() <= 0)
        {
            return defaultvalue;
        }
        if (version == null || version.length() <= 0)
        {
            version = DEFAULT_KEY_VERSION;
        }
        return sharedPreferences.getLong(version + "|" + key, defaultvalue);
    }

    /**
     * 取一个boolean
     * 
     * @param key
     *            key
     * @param defaultvalue
     *            默认值
     * @return key对应的boolean
     */
    public boolean read(String key, boolean defaultvalue)
    {
        return read(DEFAULT_KEY_VERSION, key, defaultvalue);
    }

    /**
     * 取一个boolean
     * 
     * @param version
     *            key版本
     * @param key
     *            key
     * @param defaultvalue
     *            默认值
     * @return key对应的boolean
     */
    public boolean read(String version, String key, boolean defaultvalue)
    {
        if (key == null || key.length() <= 0)
        {
            return defaultvalue;
        }
        if (version == null || version.length() <= 0)
        {
            version = DEFAULT_KEY_VERSION;
        }
        return sharedPreferences.getBoolean(version + "|" + key, defaultvalue);
    }

    /**
     * 取一个float
     * 
     * @param key
     *            key
     * @param defaultvalue
     *            默认值
     * @return key对应的float
     */
    public float read(String key, float defaultvalue)
    {
        return read(DEFAULT_KEY_VERSION, key, defaultvalue);
    }

    /**
     * 取一个float
     * 
     * @param version
     *            key版本
     * @param key
     *            key
     * @param defaultvalue
     *            默认值
     * @return key对应的float
     */
    public float read(String version, String key, float defaultvalue)
    {
        if (key == null || key.length() <= 0)
        {
            return defaultvalue;
        }
        if (version == null || version.length() <= 0)
        {
            version = DEFAULT_KEY_VERSION;
        }
        return sharedPreferences.getFloat(version + "|" + key, defaultvalue);
    }

    /**
     * 取出xml中全部内容
     * 
     * @return Map<String,?>
     */
    public Map<String, ?> readAllValues()
    {
        return sharedPreferences.getAll();
    }

    /**
     * 取出默认版本全部缓存
     * 
     * @return Map<String,?>
     */
    public Map<String, ?> readAll()
    {
        return readAll(DEFAULT_KEY_VERSION);
    }

    /**
     * 取出当前版本全部缓存
     * 
     * @param version
     * @return
     */
    public Map<String, Object> readAll(String version)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        if (version == null || version.length() <= 0)
        {
            version = DEFAULT_KEY_VERSION;
        }
        Map<String, ?> all = readAllValues();
        if (all == null || all.size() <= 0)
        {
            return result;
        }
        for (String key : all.keySet())
        {
            String[] arr = key.split("\\|");
            if (arr[0].equals(version))
            {
                result.put(arr[1], all.get(key));
            }
        }
        return result;
    }

    /**
     * 默认版本是否存在当前key
     * 
     * @param key
     *            key
     * @return boolean
     */
    public boolean isContains(String key)
    {
        return isContains(DEFAULT_KEY_VERSION, key);
    }

    /**
     * 是否存在当前版本的key
     * 
     * @param version
     *            版本
     * @param key
     *            key
     * @return boolean
     */
    public boolean isContains(String version, String key)
    {
        if (key == null || key.length() <= 0)
        {
            return false;
        }
        if (version == null || version.length() <= 0)
        {
            version = DEFAULT_KEY_VERSION;
        }
        return sharedPreferences.contains(version + "|" + key);
    }

    /**
     * 删除默认version key
     * 
     * @param key
     */
    public void remove(String key)
    {
        remove(DEFAULT_KEY_VERSION, key);
    }

    /**
     * 删除某一version key
     * 
     * @param version
     * @param key
     */
    public void remove(String version, String key)
    {
        if (key == null || key.length() <= 0)
        {
            return;
        }
        if (version == null || version.length() <= 0)
        {
            version = DEFAULT_KEY_VERSION;
        }
        Editor editor = sharedPreferences.edit();
        editor.remove(version + "|" + key);
        apply(editor);
    }

    /**
     * 获取当前key的所有版本号，性能较差谨慎使用
     * 
     * @param key
     * @return 返回当前key的所有版本号，list的size为0表示不存在当前key
     */
    public List<String> getVersions(String key)
    {
        if (TextUtils.isEmpty(key))
        {
            return new ArrayList<String>();
        }
        List<String> versionlist = new ArrayList<String>();
        Set<String> keys = readAllValues().keySet();
        for (String k : keys)
        {
            String[] arr = k.split("\\|");
            if (arr.length >= 2)
            {
                if (key.equals(arr[1]))
                {
                    versionlist.add(arr[0]);
                }
            }
        }
        return new ArrayList<String>();
    }

    /**
     * 删除当前key下所有版本的值，性能较差谨慎使用
     * 
     * @param key
     */
    public void removeAllVersionValues(String key)
    {
        if (key == null || key.length() <= 0)
        {
            return;
        }
        List<String> list = getVersions(key);
        if (list.size() > 0)
        {
            Editor editor = sharedPreferences.edit();
            for (String v : list)
            {
                editor.remove(v + "|" + key);
            }
            apply(editor);
        }
    }

    /**
     * 清空xml下所有值
     */
    public void removeAll()
    {
        Editor editor = sharedPreferences.edit();
        editor.clear();
        apply(editor);
    }

    @SuppressLint("NewApi")
    private void apply(Editor editor)
    {
        if (VERSION.SDK_INT >= 9)
            editor.apply();
        else
            editor.commit();

    }

}
