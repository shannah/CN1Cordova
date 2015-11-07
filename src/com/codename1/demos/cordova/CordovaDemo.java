package com.codename1.demos.cordova;


import com.codename1.cordova.CordovaApplication;
import com.codename1.demos.cordova.plugins.camera.CameraPlugin;
import com.codename1.io.Log;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import java.io.IOException;

public class CordovaDemo {

    private Form current;
    private Resources theme;
    private CordovaApplication app;

    public void init(Object context) {
        theme = UIManager.initFirstTheme("/theme");

        // Pro users - uncomment this code to get crash reports sent to you automatically
        /*Display.getInstance().addEdtErrorHandler(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                evt.consume();
                Log.p("Exception in AppName version " + Display.getInstance().getProperty("AppVersion", "Unknown"));
                Log.p("OS " + Display.getInstance().getPlatformName());
                Log.p("Error " + evt.getSource());
                Log.p("Current Form " + Display.getInstance().getCurrent().getName());
                Log.e((Throwable)evt.getSource());
                Log.sendLog();
            }
        });*/
    }
    
    public void start() {
        if(current != null){
            app.resume();
            current.show();
            return;
        }
        app = new CordovaApplication();
        app.addPlugin("Camera", new CameraPlugin());
        try {
            app.load("index.html");
        } catch (Exception ex) {
            Log.e(ex);
        }
        app.show();
    }

    public void stop() {
        current = Display.getInstance().getCurrent();
        app.pause();
    }
    
    public void destroy() {
    }

}
