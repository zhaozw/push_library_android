package com.qding.push;

import android.app.Activity;
import android.app.Application;
import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Created by n.d on 2017/5/5.
 */

public class PushManager {
    private final static String ERROR = "ERROR";

    private static String tokenOther;
    private static String tokenHuawei;
    private static String tokenMi;

    @Constants.OSType
    private static int OS = Constants.OS_OTHER;

    //message
    private static ArrayList<PushListener> pushListeners = new ArrayList<>();
    //token
    private static ArrayList<TokenListener> tokenListeners = new ArrayList<>();

//    private static int reportTime = 1;

    public static void init(Application context) {
        // decide kind of os.
        String packageName = context.getPackageName();
        if (Util.checkProcess(context, packageName, packageName + Constants.PROCESS_CHANNEL)) {
            // init UM.
            UmengManager.init(context);

        }
    }

    public static void onCreate(Activity context, TokenListener listener) {
        regTokenListener(listener);
        OS = Util.checkOS(context);
        if (OS == Constants.OS_HUAWEI) {
            //active hw
            HuaweiManager.init(context);
        } else if (OS == Constants.OS_MI) {
            // active MI
            MiManager.init(context);
        }
        checkToken();
    }

    public static void onStart(Activity context) {
        if (OS == Constants.OS_HUAWEI) {
            HuaweiManager.connect(context);
        }
        UmengManager.onStart();
    }


    private static void regMessageListener(PushListener listener) {
        if ((listener != null) && (!pushListeners.contains(listener))) {
            pushListeners.add(listener);
        }
    }

    private static void regTokenListener(TokenListener listener) {
        if ((listener != null) && (!tokenListeners.contains(listener))) {
            tokenListeners.add(listener);
        }
    }


    static void gotToken(@Constants.OSType int type, String token) {
        if (TextUtils.isEmpty(token)) {
            token = ERROR;
        }
        if (type == Constants.OS_OTHER) {
            tokenOther = token;
        } else if (type == Constants.OS_HUAWEI) {
            tokenHuawei = token;
        } else if (type == Constants.OS_MI) {
            tokenMi = token;
        }
        checkToken();
    }

    private static void checkToken() {
        //default value
        int type = -1;
        String token = null;
        if (OS == Constants.OS_HUAWEI) {
            if (!TextUtils.isEmpty(tokenHuawei)) {
                if (ERROR.equals(tokenHuawei)) {
                    // other
                    type = Constants.OS_OTHER;
                    token = tokenOther;
                } else if (!TextUtils.isEmpty(tokenHuawei)) {
                    // hauwei
                    type = Constants.OS_HUAWEI;
                    token = tokenHuawei;
                }
            }
        } else if (OS == Constants.OS_MI) {
            if (!TextUtils.isEmpty(tokenMi)) {
                if (ERROR.equals(tokenMi)) {
                    // other
                    type = Constants.OS_OTHER;
                    token = tokenOther;
                } else if (!TextUtils.isEmpty(tokenMi)) {
                    // MI
                    type = Constants.OS_MI;
                    token = tokenMi;
                }
            }
        } else {
            if (!TextUtils.isEmpty(tokenOther)) {
                // other
                type = Constants.OS_OTHER;
                token = tokenOther;
            }
        }
        if ( tokenListeners.size() > 0 && !TextUtils.isEmpty(token) && !ERROR.equals(token)) {
//            reportTime--;
            for (TokenListener listener : tokenListeners) {
                listener.gotToken(type, token);
            }
        }
    }

    public static void unreglistener(TokenListener listener) {
        tokenListeners.remove(listener);
    }

}