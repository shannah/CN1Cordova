/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.cordova;

import com.codename1.javascript.JSObject;
import com.codename1.util.Callback;
import com.codename1.xml.Element;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shannah
 */
public interface CordovaPlugin {
    public boolean execute(String action, List args, Callback callback);
    public boolean execute(String action, String jsonArgs, Callback callback);
        
    
}
