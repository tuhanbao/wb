package com.tuhanbao.util.config;

import java.io.IOException;
import java.util.Map;

import com.tuhanbao.io.base.Constants;
import com.tuhanbao.io.base.KeyValueBean;
import com.tuhanbao.io.objutil.StringUtil;
import com.tuhanbao.io.txt.util.TxtUtil;
import com.tuhanbao.util.exception.BaseErrorCode;
import com.tuhanbao.util.exception.MyException;
import com.tuhanbao.util.log.LogManager;

public class Config
{
    private KeyValueBean bean;
    
    public Config(String url)
    {
        this(url, Constants.EQUAL);
    }

    public Config(KeyValueBean bean)
    {
        this.bean = bean;
    }
    
    public Config(String url, String keyValueGap)
    {
        try
        {
            bean = new KeyValueBean(TxtUtil.read(url), Constants.ENTER, keyValueGap);
        }
        catch (IOException e)
        {
            LogManager.error(e);
            throw new MyException(BaseErrorCode.INIT_CONFIGFILE_ERROR, url);
        }
    }
    
    public void addProperties(KeyValueBean bean) {
        if (this.bean == null) this.bean = bean;
        else {
            this.bean.addProperties(bean);
        }
    }
    
    public String getString(String key)
    {
        return bean.get(key);
    }
    
    public int getInt(String key)
    {
        return Integer.valueOf(bean.get(key));
    }
    
    public boolean getBoolean(String key)
    {
        return Integer.valueOf(bean.get(key)) == 1;
    }
    
    public String[] getStringArray(String key)
    {
        return StringUtil.string2Array(getString(key));
    }
    
    public int[] getIntArray(String key)
    {
        String[] array = this.getStringArray(key);
        int length = array.length;
        int[] result = new int[length];
        for (int i = 0; i < length; i++)
        {
            result[i] = Integer.valueOf(array[i].trim());
        }
        return result;
    }
    
    public Map<String, String> getProperties()
    {
        return bean.getProperties();
    }
    
    public boolean containsKey(String key) {
    	return bean.containsKey(key);
    }
}
