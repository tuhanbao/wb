package com.tuhanbao.util.exception;

import com.tuhanbao.io.base.Constants;
import com.tuhanbao.util.config.Config;
import com.tuhanbao.util.config.ConfigManager;
import com.tuhanbao.util.config.ConfigRefreshListener;

public final class ErrorCodeMsgManager implements ConfigRefreshListener {

    private static final String KEY = "errorCode";
    
    private static Config config = null;

    /**
     * 先简单实现，之后可以通过错误码配置文件实现
     * 
     * @param errCode
     * @return
     */
    public static final String getErrMsg(int errCode, String ... args)
    {
        if (config == null) init();
        String msg = config.getString(errCode + "");
        
        if (args == null) return msg;
        int length = args.length;
        //未用到的参数拼在末尾
        StringBuilder noUseArgs = new StringBuilder();
        for (int i = 0; i < length; i++) {
            String target = "{" + i + "}";
            if (msg.contains(target)) {
                msg = msg.replace(target, args[i]);
            }
            else {
                noUseArgs.append(args[i]).append(Constants.COMMA);
            }
        }
        if (noUseArgs.length() > 0) {
            noUseArgs.deleteCharAt(noUseArgs.length() - 1);
            return msg + "(" + noUseArgs + ")";
        }
        else {
            return msg;
        }
    }
    
    private static final void init() {
        config = ConfigManager.getConfig(KEY);
        if (config == null) {
            throw new MyException(BaseErrorCode.INIT_CONFIGFILE_ERROR, KEY);
        }
    }

    @Override
    public void refresh() {
        init();
    }

    @Override
    public String getKey() {
        return KEY;
    }

}
