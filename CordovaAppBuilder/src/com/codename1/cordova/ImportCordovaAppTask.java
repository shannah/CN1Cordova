/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.cordova;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Replace;
import org.apache.tools.ant.taskdefs.Replace.NestedString;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author shannah
 */
public class ImportCordovaAppTask extends Task {

    @Override
    public void execute() throws BuildException {
        
        String source = getProject().getProperty("source");
        if (source == null) {
            throw new BuildException("No source path was provided.  Please set the source path for the application to be imported.  -Dsource=<path>");
        }
        
        File sourceDir = new File(source);
        if (!sourceDir.exists()) {
            throw new BuildException("Source path provided ("+sourceDir+") doesn't exist.");
        }
        
        if (!sourceDir.isDirectory()) {
            throw new BuildException(sourceDir+" is not a directory");
        }
        
        File configXml = new File(sourceDir, "config.xml");
        if (!configXml.exists()) {
            throw new BuildException(sourceDir+" does not appear to be a valid cordova project.  It is missing the config.xml file");
        }
        
        Element config = null;
        try {
            config = loadConfigFile(configXml);
        } catch (Exception ex) {
            throw new BuildException("Failed to parse config.xml", ex);
        }
        
        String packageId = config.getAttribute("id");
        String version = config.getAttribute("version");
        String name = null;
        NodeList children = config.getChildNodes();
        int len = children.getLength();
        for (int i=0; i<len; i++) {
            Node node = children.item(i);
            if (node instanceof Element) {
                Element el = (Element)node;
                if ("name".equals(el.getTagName())) {
                    name = el.getTextContent();
                } else if ("plugin".equals(el.getTagName())) {
                    log("The plugin "+el.getAttribute("name")+" was not automatically imported.  Please install the equivalent plugin as a .cn1lib");
                }
            }
        }
        
        
        File wwwDir = new File(sourceDir, "www");
        if (!wwwDir.exists()) {
            throw new BuildException("Expected to find www directory at "+wwwDir+" but found none.");
        }
        
        File srcDir = new File(getProject().getBaseDir(), "src");
        File htmlDir = new File(srcDir, "html");
        if (htmlDir.exists()) {
            File backupDir = new File(getProject().getBaseDir(), "backups");
            backupDir.mkdir();
            File backupSnapshot = new File(backupDir, "html"+System.currentTimeMillis());
            htmlDir.renameTo(backupSnapshot);
        }
        
        
        Copy copyWWW = (Copy)getProject().createTask("copy");
        FileSet wwwFS = new FileSet();
        wwwFS.setDir(wwwDir);
        wwwFS.setIncludes("**");
        copyWWW.addFileset(wwwFS);
        
        htmlDir.mkdir();
        copyWWW.setTodir(htmlDir);
        copyWWW.execute();
        
        Properties codenameOneSettings = new Properties();
        File codenameOneSettingsFile = new File(getProject().getBaseDir(), "codenameone_settings.properties");
        if (!codenameOneSettingsFile.exists()) {
            throw new BuildException("No codenameone_settings.properties file found at "+codenameOneSettingsFile);
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(codenameOneSettingsFile);
            codenameOneSettings.load(fis);
        } catch (Exception ex) {
            throw new BuildException("Failed to load "+codenameOneSettingsFile, ex);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception ex) {
                    
                }
            }
        }
        
        codenameOneSettings.setProperty("codename1.version", version);
        
        String oldPackage = codenameOneSettings.getProperty("codename1.packageName");
        
        
        codenameOneSettings.setProperty("codename1.packageName", packageId);
        codenameOneSettings.setProperty("codename1.displayName", name);
        String iosAppId = codenameOneSettings.getProperty("codename1.ios.appid");
        if (iosAppId == null) {
            iosAppId = "BQ5FVWYLLB.com.codename1.demos.cordova";
        }
        iosAppId = iosAppId.substring(0, iosAppId.indexOf("."))+"."+packageId;
        codenameOneSettings.setProperty("codename1.ios.appid", iosAppId);
        
        if (!packageId.equals(oldPackage)) {
            String oldMainPath = oldPackage.replace(".", File.separator);
            String newMainPath = packageId.replace(".", File.separator);
            File oldMainFile = new File(srcDir, oldMainPath);
            File newMainFile = new File(srcDir, newMainPath);
            
            if (!oldMainFile.exists()) {
                throw new BuildException("Could not find old main file: "+oldMainFile);
            }
            
            File oldMainDir = oldMainFile.getParentFile();
            File newMainDir = newMainFile.getParentFile();
            
            newMainDir.getParentFile().mkdirs();
            if (newMainDir.exists()) {
                for (File f : oldMainDir.listFiles()) {
                    if (".".equals(f.getName()) || "..".equals(f.getName())) {
                        continue;
                    }
                    f.renameTo(new File(newMainDir, f.getName()));
                }
                oldMainDir.delete();
            } else {
            
                oldMainDir.renameTo(newMainDir);
            }
            
            Replace replaceTask = (Replace)getProject().createTask("replace");
            replaceTask.setDir(srcDir);
            replaceTask.setIncludes("**/*.java");
            NestedString token = replaceTask.createReplaceToken();
            token.addText(oldPackage);
            
            replaceTask.createReplaceValue().addText(packageId);
            replaceTask.execute();
        }
        
        
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(codenameOneSettingsFile);
            codenameOneSettings.store(fos, "Updated by ImportCordovaAppTask "+new Date());
        } catch (Exception ex) {
            throw new BuildException("Failed to save changes to "+codenameOneSettingsFile, ex);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception ex){}
            }
        }
        
        
        
        File nbprojectDir = new File(getProject().getBaseDir(), "nbproject");
        
        File nbprojectPropertiesFile = new File(nbprojectDir, "project.properties");
        if (nbprojectPropertiesFile.exists()) {
            Properties nbprojectProperties = new Properties();
            log("Updating Netbeans properties file "+nbprojectPropertiesFile);
            fis = null;
            try {
                fis = new FileInputStream(nbprojectPropertiesFile);
                nbprojectProperties.load(fis);
            } catch (Exception ex) {
                throw new BuildException("Failed to load netbeans project properties at "+nbprojectPropertiesFile);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Exception ex){}
                }
            }
            
            String normalizedName = name.replaceAll("[^a-zA-Z0-9]", "");
            
            nbprojectProperties.setProperty("application.title", name);
            nbprojectProperties.setProperty("dist.jar", "${dist.dir}/"+normalizedName+".jar");
            
            fos = null;
            try {
                fos = new FileOutputStream(nbprojectPropertiesFile);
                nbprojectProperties.store(fos, "Updated by ImportCordovaAppTask "+new Date());
            } catch (Exception ex) {
                throw new BuildException("Failed to save changes to "+nbprojectPropertiesFile, ex);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception ex){}
                }
            }
            
            
            Replace replaceProjectName = (Replace)getProject().createTask("replace");
            replaceProjectName.setFile(new File(nbprojectDir, "project.xml"));
            replaceProjectName.createReplaceToken().addText("<name>CordovaProjectTemplate</name>");
            replaceProjectName.createReplaceValue().addText("<name>"+name.replace("<", "&lt;").replace(">", "&gt;")+"</name>");
            replaceProjectName.execute();
        }
        log("Successfully imported cordova project at "+sourceDir);
    }
    
    
    private Element loadConfigFile(File configXml) throws SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new BuildException(ex);
        }
        Document config = null;
        config = dBuilder.parse(configXml);
        return config.getDocumentElement();
    }
}
