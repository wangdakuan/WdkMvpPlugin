package com.wdk;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.*;

public class MvpDataCreateAction extends AnAction {


    private Project project;

    VirtualFile selectGroup;

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        Presentation presentation = event.getPresentation();
        if (isRightPackage(event)) {
            presentation.setEnabledAndVisible(true);
        } else {
            presentation.setEnabledAndVisible(false);
        }
    }


    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getProject();
//        packageName = getPackageName();
        String className = Messages.showInputDialog(project, "请输入类名称", "新建模板", Messages.getQuestionIcon());
        if (isEmpty(className == null, className.equals(""))) {
            Messages.showErrorDialog(
                    "You have to type in something.",
                    "content is empty");
            return;
        }
        createClassMvp(className);
        project.getBaseDir().refresh(false, true);
    }

    private boolean isEmpty(boolean b, boolean equals) {
        return b || equals;
    }


    private boolean isRightPackage(AnActionEvent actionEvent) {
        selectGroup = DataKeys.VIRTUAL_FILE.getData(actionEvent.getDataContext());
        String packageName = selectGroup.getPath();
        System.out.println("packageName" + packageName);
        if (packageName == null || packageName.equals(""))
            return false;
        String[] subPackages = packageName.split("\\/");
        for (String subPackage : subPackages) {
            if (subPackage.endsWith("java")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据模块生成代码
     *
     * @param className
     */
    private void createClassMvp(String className) {
        boolean isFragment = isEmpty(className.endsWith("Fragment"), className.toLowerCase().endsWith("fragment"));
        boolean isActivity = isEmpty(className.endsWith("Activity"), className.toLowerCase().endsWith("activity"));
        if (className.toLowerCase().endsWith("fragment")|| className.toLowerCase().endsWith("activity")) {
            className = className.substring(0, className.length() - 8);
        }

        if (isFragment) {
            String fragment = readFile("Fragment.txt")
                    .replace("&package&", getPackageName(selectGroup.getPath()))
                    .replace("&Fragment&", className + "Fragment");
            writetoFile(fragment, selectGroup.getPath(), className + "Fragment.java");
        } else if (isActivity) {
            String activity = readFile("Activity.txt")
                    .replace("&package&", getPackageName(selectGroup.getPath()))
                    .replace("&Activity&", className + "Page");
            writetoFile(activity, selectGroup.getPath(), className + "Page.java");
        }
        boolean isData = isEmpty(className.endsWith("DataMvp"), className.toLowerCase().endsWith("datamvp"));
        if (isData) {
            if (className.toLowerCase().endsWith("datamvp")) {
                className = className.substring(0, className.length() - 7);
            }
            String basePath = selectGroup.getPath() + "/" + className.toLowerCase();
            String dataPath = project.getBasePath() + "/dodata/src/main/java/com/dodata/" + className + "";
            String interactorPackageName = getPackageName(basePath) + ".interactor";
            String factoryPackageName = getPackageName(basePath) + ".factory";
            String sourcePackageName = getPackageName(basePath) + ".source";
            String serverPackageName = getPackageName(basePath) + ".server";
            String factoryPath = selectGroup.getPath() + "/" + className.toLowerCase() + "/factory";
            String sourcePath = selectGroup.getPath() + "/" + className.toLowerCase() + "/source";
            String serverPath = selectGroup.getPath() + "/" + className.toLowerCase() + "/server";
            String interactorPath = selectGroup.getPath() + "/" + className.toLowerCase() + "/interactor";

            String diaskSource = readFile("DiaskSource.txt")
                    .replace("&package&", sourcePackageName)
                    .replace("&Module&", className)
                    .replace("&InteractorPackageName&", interactorPackageName);
            writetoFile(diaskSource, sourcePath, className + "DiaskSource.java");

            String source = readFile("Source.txt")
                    .replace("&package&", sourcePackageName)
                    .replace("&Module&", className)
                    .replace("&InteractorPackageName&", interactorPackageName);
            writetoFile(source, sourcePath, className + "Source.java");

            String factory = readFile("Factory.txt")
                    .replace("&package&", factoryPackageName)
                    .replace("&Module&", className)
                    .replace("&InteractorPackageName&", interactorPackageName)
                    .replace("&SourcePackageName&", sourcePackageName)
                    .replace("&DiaskSourcePackageName&", sourcePackageName);
            writetoFile(factory, factoryPath, className + "Factory.java");

            String interactor = readFile("Interactor.txt")
                    .replace("&package&", interactorPackageName)
                    .replace("&Module&", className);
            writetoFile(interactor, interactorPath, className + "Interactor.java");

            String server = readFile("Server.txt")
                    .replace("&package&", serverPackageName)
                    .replace("&FactoryPackageName&", factoryPackageName)
                    .replace("&InteractorPackageName&", interactorPackageName)
                    .replace("&Module&", className);
            writetoFile(server, serverPath, className + "Server.java");


        }
        boolean isView = isEmpty(className.endsWith("ViewMvp"), className.toLowerCase().endsWith("viewmvp"));
        if (isView) {
            if (className.toLowerCase().endsWith("viewmvp")) {
                className = className.substring(0, className.length() - 7);
            }
            String basePath = selectGroup.getPath() + "/" + className.toLowerCase();
            String interactorPath = getPackageName(basePath) + "/interfaces";
            String factoryPath = getPackageName(basePath) + "/factory";
            String presenterPath = selectGroup.getPath() + "/" + className.toLowerCase();
            String viewPath = selectGroup.getPath() + "/" + className.toLowerCase() + "/viewinteractor";
            String viewPackageName = getPackageName(basePath) + ".viewinteractor";
            String view = readFile("View.txt")
                    .replace("&package&", viewPackageName)
//                    .replace("&FactoryPackageName&", factoryPath)
//                    .replace("&InteractorPackageName&", interactorPath)
                    .replace("&Module&", className);
            writetoFile(view, viewPath, className + "View.java");

            String presenter = readFile("Presenter.txt")
                    .replace("&package&", getPackageName(basePath))
                    .replace("&ViewPackageName&", viewPackageName)
                    .replace("&Module&", className)
                    .replace("&ModuleName&", className.toLowerCase());
            writetoFile(presenter, presenterPath, className + "Presenter.java");
        }
    }

    private String getPackageName(String path) {
        return path.substring(path.indexOf("java") + 5, path.length()).replace("/", ".");
    }

    private String readFile(String filename) {
        InputStream in = null;
        in = this.getClass().getResourceAsStream("template/" + filename);
        String content = "";
        try {
            content = new String(readStream(in));
        } catch (Exception e) {
        }
        return content;
    }

    private void writetoFile(String content, String filepath, String filename) {
        try {
            File floder = new File(filepath);
            // if file doesnt exists, then create it
            if (!floder.exists()) {
                floder.mkdirs();
            }
            File file = new File(filepath + "/" + filename);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }

        } catch (IOException e) {
        } finally {
            outSteam.close();
            inStream.close();
        }
        return outSteam.toByteArray();
    }

}
